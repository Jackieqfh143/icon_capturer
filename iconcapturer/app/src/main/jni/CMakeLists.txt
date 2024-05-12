#project(native-lib)
cmake_minimum_required(VERSION 3.10)
# Sets lib_src_DIR to the path of the target CMake project.
#set( lib_ocr_DIR ${CMAKE_SOURCE_DIR}/ocr )

# Sets lib_build_DIR to the path of the desired output directory.
#set( lib_ocr_build_DIR ${CMAKE_SOURCE_DIR}/ocr/outputs )
#file(MAKE_DIRECTORY ${lib_ocr_build_DIR})
#
# Adds the CMakeLists.txt file located in the specified directory
# as a build dependency.
#add_subdirectory( # Specifies the directory of the CMakeLists.txt file.
#        ${lib_ocr_DIR})

        # Specifies the directory for the build outputs.
#        ${lib_ocr_build_DIR} )

# Adds the output of the additional CMake build as a prebuilt static
# library and names it lib_gmath.
#add_library( lib_ocr SHARED IMPORTED )
#set_target_properties( lib_ocr PROPERTIES IMPORTED_LOCATION
#        ${lib_ocr_build_DIR}/${ANDROID_ABI}/lib_ocr.so )
#include_directories( ${lib_ocr_DIR}/include )


# Sets lib_src_DIR to the path of the target CMake project.
set(lib_square_DIR ${CMAKE_SOURCE_DIR}/square )

# Sets lib_build_DIR to the path of the desired output directory.
#set( lib_square_build_DIR ${CMAKE_SOURCE_DIR}/outputs )
#file(MAKE_DIRECTORY ${lib_square_build_DIR})

# Adds the CMakeLists.txt file located in the specified directory
# as a build dependency.
add_subdirectory( # Specifies the directory of the CMakeLists.txt file.
        ${lib_square_DIR})

        # Specifies the directory for the build outputs.
#        ${lib_square_build_DIR} )

# Adds the output of the additional CMake build as a prebuilt static
# library and names it lib_gmath.
#add_library( lib_square SHARED IMPORTED )
#set_target_properties( lib_square PROPERTIES IMPORTED_LOCATION
#        ${lib_square_build_DIR}/${ANDROID_ABI}/lib_square.so )
#include_directories( ${lib_square_DIR}/include )

# Links the top-level CMake build output against lib_gmath.
#target_link_libraries( native-lib ... lib_ocr lib_square)