cmake_minimum_required(VERSION 3.15)
project(valor_injector LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)

# Build as a Windows DLL
add_library(valor_injector SHARED valor_injector.cpp)

# Link Windows libraries required for process manipulation and console allocation
target_link_libraries(valor_injector PRIVATE user32 kernel32)

# Ensure proper include paths for JDK (JNI & JVMTI headers)
if(DEFINED ENV{JAVA_HOME})
    target_include_directories(valor_injector PRIVATE 
        "$ENV{JAVA_HOME}/include"
        "$ENV{JAVA_HOME}/include/win32"
    )
else()
    find_package(JNI)
    if(JNI_FOUND)
        target_include_directories(valor_injector PRIVATE ${JNI_INCLUDE_DIRS})
    endif()
endif()

# Post-build copy to build/libs and src/main/resources
add_custom_command(TARGET valor_injector POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E make_directory "${CMAKE_CURRENT_SOURCE_DIR}/../build/libs"
    COMMAND ${CMAKE_COMMAND} -E copy_if_different "$<TARGET_FILE:valor_injector>" "${CMAKE_CURRENT_SOURCE_DIR}/../build/libs/"
    COMMAND ${CMAKE_COMMAND} -E make_directory "${CMAKE_CURRENT_SOURCE_DIR}/../src/main/resources"
    COMMAND ${CMAKE_COMMAND} -E copy_if_different "$<TARGET_FILE:valor_injector>" "${CMAKE_CURRENT_SOURCE_DIR}/../src/main/resources/"
)

