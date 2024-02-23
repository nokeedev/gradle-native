#pragma once

#ifdef _WIN32
#define EXPORT_FUNC __declspec(dllexport)
#else
#define EXPORT_FUNC
#endif

#include <string.h>

EXPORT_FUNC char * say_hello(const char * name);
