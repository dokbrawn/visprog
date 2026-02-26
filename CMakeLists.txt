cmake_minimum_required(VERSION 3.14)
project(imgui_demo LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)


add_definitions(-D_CRT_SECURE_NO_WARNINGS)


find_package(SDL2 REQUIRED)
find_package(GLEW REQUIRED)
find_package(OpenGL REQUIRED)
find_package(cppzmq REQUIRED)
find_package(nlohmann_json REQUIRED)
find_package(ZeroMQ REQUIRED)


add_library(imgui STATIC
    third_party/imgui/imgui.cpp
    third_party/imgui/imgui_draw.cpp
    third_party/imgui/imgui_demo.cpp
    third_party/imgui/imgui_tables.cpp
    third_party/imgui/imgui_widgets.cpp
    third_party/imgui/backends/imgui_impl_sdl2.cpp
    third_party/imgui/backends/imgui_impl_opengl3.cpp
)
target_include_directories(imgui PUBLIC third_party/imgui)
target_link_libraries(imgui PRIVATE SDL2::SDL2 GLEW::GLEW OpenGL::GL)


add_library(implot STATIC
    third_party/implot/implot.cpp
    third_party/implot/implot_items.cpp
)
target_include_directories(implot PUBLIC third_party/implot third_party/imgui)
target_link_libraries(implot PRIVATE imgui)


add_executable(main src/main.cpp)


target_link_libraries(main PRIVATE 
    implot
    imgui
    SDL2::SDL2
    GLEW::GLEW
    OpenGL::GL
    cppzmq
    nlohmann_json::nlohmann_json
    libzmq                      
)

target_include_directories(main PRIVATE ${CMAKE_SOURCE_DIR}/src)