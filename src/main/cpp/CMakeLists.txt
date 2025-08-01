cmake_minimum_required(VERSION 3.28)
project(velox4jcpp)

include(FetchContent)

# Preconditions.
if(NOT UNIX)
  message(
    FATAL_ERROR
      "The project velox4j-cpp only supports UNIX-based systems at the moment.")
endif()

# Compile options.
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED TRUE)
set(CMAKE_CXX_EXTENSIONS ON)
set(CMAKE_POSITION_INDEPENDENT_CODE TRUE)

# Velox4J option definitions.
option(VELOX4J_ENABLE_CCACHE "Use ccache if installed." ON)
option(VELOX4J_BUILD_TESTING "Build Velox4J tests." ON)

# Velox4J non-bool option definitions.
set(VELOX4J_INSTALL_DESTINATION
    "lib"
    CACHE
      STRING
      "The directory Velox4J's libraries are installed. This is relative to the value of CMAKE_INSTALL_PREFIX."
)

# Setup JNI.
find_package(Java REQUIRED)
find_package(JNI REQUIRED)
if(JNI_FOUND)
  message(STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
  message(STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif()
include(UseJava)

# Set Velox options.
set(VELOX_DEPENDENCY_SOURCE AUTO)
set(folly_SOURCE BUNDLED) # Use velox-bundled folly because we have a custom
                          # patch for folly setup.
set(VELOX_ENABLE_CCACHE ON)
set(VELOX_MONO_LIBRARY ON)
set(VELOX_BUILD_SHARED ON)
set(VELOX_BUILD_STATIC OFF)
set(VELOX_ENABLE_ARROW ON)
if(NOT VELOX4J_BUILD_TESTING)
  set(VELOX_BUILD_TESTING OFF)
endif()
if(NOT VELOX4J_ENABLE_CCACHE)
  set(VELOX_ENABLE_CCACHE OFF)
endif()

# Import Velox.
set(VELOX_PATCH_DIR ${CMAKE_CURRENT_LIST_DIR}/cmake/patches/velox)
set(VELOX_REF_FILE ${CMAKE_CURRENT_LIST_DIR}/velox-ref.txt)
set(VELOX_REF_MD5_FILE ${CMAKE_CURRENT_LIST_DIR}/velox-ref-md5.txt)
set(VELOX_REPO facebookincubator/velox)
file(STRINGS ${VELOX_REF_FILE} VELOX_REF)
file(STRINGS ${VELOX_REF_MD5_FILE} VELOX_SOURCE_URL_MD5)
set(VELOX_SOURCE_URL
    "https://github.com/${VELOX_REPO}/archive/${VELOX_REF}.zip")
FetchContent_Declare(
  velox
  URL ${VELOX_SOURCE_URL}
  URL_HASH MD5=${VELOX_SOURCE_URL_MD5})
FetchContent_MakeAvailable(velox)

# Import JniHelpers.
set(JNI_HELPERS_REPO spotify/JniHelpers)
set(JNI_HELPERS_REF 956649eb352252ff6c43362d9b9c526f9241e480)
FetchContent_Declare(
  JniHelpers
  GIT_REPOSITORY https://github.com/${JNI_HELPERS_REPO}.git
  GIT_TAG ${JNI_HELPERS_REF} # release-1.10.0
)
FetchContent_MakeAvailable(JniHelpers)

# Directories of Velox4J.
set(VELOX4J_SOURCE_DIR ${CMAKE_CURRENT_LIST_DIR}/main)
set(VELOX4J_TEST_DIR ${CMAKE_CURRENT_LIST_DIR}/test)

# Setup compiler launcher to ccache if using similar code with Velox.
if(VELOX4J_ENABLE_CCACHE
   AND NOT CMAKE_C_COMPILER_LAUNCHER
   AND NOT CMAKE_CXX_COMPILER_LAUNCHER)
  find_program(CCACHE_FOUND ccache)
  if(CCACHE_FOUND)
    message(STATUS "Using ccache: ${CCACHE_FOUND}")
    set(CMAKE_C_COMPILER_LAUNCHER ${CCACHE_FOUND})
    set(CMAKE_CXX_COMPILER_LAUNCHER ${CCACHE_FOUND})
    # keep comments as they might matter to the compiler
    set(ENV{CCACHE_COMMENTS} "1")
  endif()
endif()

# Setup compiler flags using Velox's provided script.
execute_process(
  COMMAND
    bash -c
    "( source ${velox_SOURCE_DIR}/scripts/setup-helper-functions.sh && echo -n $(get_cxx_flags $ENV{CPU_TARGET}))"
  OUTPUT_VARIABLE SCRIPT_CXX_FLAGS
  RESULT_VARIABLE COMMAND_STATUS)
if(COMMAND_STATUS EQUAL "1")
  message(FATAL_ERROR "Unable to determine compiler flags!")
endif()
message("Setting CMAKE_CXX_FLAGS=${SCRIPT_CXX_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${SCRIPT_CXX_FLAGS}")

# Continue to build.
add_subdirectory(main)
if(VELOX4J_BUILD_TESTING)
  add_subdirectory(test)
endif()
