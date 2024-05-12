project(findSquare)

cmake_minimum_required(VERSION 3.10)
set(OpenCV_DIR ../opencv-mobile-3.4.18-android/sdk/native/jni)
find_package(OpenCV REQUIRED core imgproc highgui)

add_library(findSquare SHARED src/squares.cpp src/jni_interface.cpp src/utils.cpp)

target_link_libraries(findSquare ${OpenCV_LIBS})
