cmake_minimum_required(VERSION 3.22.1)
project(worldboxloader)

add_library(worldboxloader SHARED native-lib.cpp)

find_library(log-lib log)
target_link_libraries(worldboxloader ${log-lib})
