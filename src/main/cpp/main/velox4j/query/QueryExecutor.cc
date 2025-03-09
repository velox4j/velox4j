/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "QueryExecutor.h"
#include <velox/exec/Task.h>
#include "velox4j/query/Query.h"

#include <utility>

namespace velox4j {

using namespace facebook::velox;

namespace {

class Out : public UpIterator {
 public:
  Out(MemoryManager* memoryManager, std::string queryJson)
      : memoryManager_(memoryManager), queryJson_(std::move(queryJson)) {
    static std::atomic<uint32_t> executionId{
        0}; // Velox query ID, same with taskId.
    const uint32_t eid = executionId++;
    auto querySerdePool = memoryManager_->getVeloxPool(
        fmt::format("Query Serde Memory Pool - EID {}", std::to_string(eid)),
        memory::MemoryPool::Kind::kLeaf);
    // Keep the pool alive until the task is finished.
    auto queryDynamic = folly::parseJson(queryJson_);
    auto query =
        ISerializable::deserialize<Query>(queryDynamic, querySerdePool);
    core::PlanFragment planFragment{
        query->plan(), core::ExecutionStrategy::kUngrouped, 1, {}};
    std::shared_ptr<core::QueryCtx> queryCtx = core::QueryCtx::create(
        nullptr,
        core::QueryConfig{query->queryConfig()->toMap()},
        query->connectorConfig()->toMap(),
        cache::AsyncDataCache::getInstance(),
        memoryManager_
            ->getVeloxPool(
                fmt::format("Query Memory Pool - EID {}", std::to_string(eid)),
                memory::MemoryPool::Kind::kAggregate)
            ->shared_from_this(),
        nullptr,
        fmt::format("Query Context - EID {}", std::to_string(eid)));

    auto task = exec::Task::create(
        fmt::format("Task - EID {}", std::to_string(eid)),
        std::move(planFragment),
        0,
        std::move(queryCtx),
        exec::Task::ExecutionMode::kSerial);

    std::unordered_set<core::PlanNodeId> planNodesWithSplits{};
    for (const auto& boundSplit : query->boundSplits()) {
      exec::Split& split = boundSplit->split();
      planNodesWithSplits.emplace(boundSplit->planNodeId());
      task->addSplit(boundSplit->planNodeId(), std::move(split));
    }
    for (const auto& nodeWithSplits : planNodesWithSplits) {
      task->noMoreSplits(nodeWithSplits);
    }

    task_ = task;

    if (!task_->supportSerialExecutionMode()) {
      VELOX_FAIL(
          "Task doesn't support single threaded execution: " +
          task->toString());
    }
  }

  ~Out() override {
    if (task_ != nullptr && task_->isRunning()) {
      // FIXME: Calling .wait() may take no effect in single thread execution
      //  mode.
      task_->requestCancel().wait();
    }
  }

  State advance() override {
    if (hasPendingState) {
      hasPendingState = false;
      return pendingState_;
    }
    VELOX_CHECK_NULL(pending_);
    if (!task_->isRunning()) {
      return State::FINISHED;
    }
    auto future = ContinueFuture::makeEmpty();
    auto out = task_->next(&future);
    saveDrivers();
    if (!future.valid()) {
      // Velox task is not blocked and a row vector should be gotten.
      if (out == nullptr) {
        return State::FINISHED;
      }
      pending_ = std::move(out);
      return State::AVAILABLE;
    }
    return State::BLOCKED;
  }

  RowVectorPtr get() override {
    VELOX_CHECK(!hasPendingState);
    VELOX_CHECK_NOT_NULL(
        pending_,
        "Out: No pending row vector to return.  No pending row vector to return. Make sure the iterator is available via member function advance() first");
    const auto out = pending_;
    pending_ = nullptr;
    return out;
  }

  void saveDrivers() {
    if (drivers_.empty()) {
      // Save driver references in the first run.
      //
      // Doing this will prevent the planned operators in drivers from being
      // destroyed together with the drivers themselves in the last call to
      // Task::next (see
      // https://github.com/facebookincubator/velox/blob/4adec182144e23d7c7d6422e0090d5b59eb32b86/velox/exec/Driver.cpp#L727C13-L727C18),
      // so if a lazy vector is not loaded while the scan is drained, a meaning
      // error
      // (https://github.com/facebookincubator/velox/blob/7af0fce2c27424fbdec1974d96bb1a6d1296419d/velox/dwio/common/ColumnLoader.cpp#L32-L35)
      // can be thrown when the vector is being loaded rather than just crashing
      // the process with "pure virtual function call" or so.
      task_->testingVisitDrivers([&](exec::Driver* driver) -> void {
        drivers_.push_back(driver->shared_from_this());
      });
      VELOX_CHECK(!drivers_.empty());
    }
  }

  MemoryManager* const memoryManager_;
  const std::string queryJson_;
  std::shared_ptr<exec::Task> task_;
  std::vector<std::shared_ptr<exec::Driver>> drivers_{};
  bool hasPendingState{false};
  State pendingState_;
  RowVectorPtr pending_{nullptr};
};
} // namespace

QueryExecutor::QueryExecutor(MemoryManager* memoryManager, std::string planJson)
    : memoryManager_(memoryManager), queryJson_(std::move(planJson)) {}

std::unique_ptr<UpIterator> QueryExecutor::execute() const {
  return std::make_unique<Out>(memoryManager_, queryJson_);
}
} // namespace velox4j
