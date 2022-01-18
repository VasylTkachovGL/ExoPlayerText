cmake_minimum_required(VERSION 3.4.1)

set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../distribution)
include_directories(${distribution_DIR}/libusb-1.0.24/libusb)
include_directories(src/main/cpp/)

find_library(log-lib log)
add_library(libusb SHARED IMPORTED)
set_target_properties(libusb PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/libusb-1.0.24/android/libs/${ANDROID_ABI}/libusb1.0.so)

add_library(native-lib SHARED
        UsbDevice.cpp
        UacDevice.cpp
        Utils.cpp
        JniUtils.cpp
        native-lib.cpp)
target_link_libraries(native-lib libusb ${log-lib})