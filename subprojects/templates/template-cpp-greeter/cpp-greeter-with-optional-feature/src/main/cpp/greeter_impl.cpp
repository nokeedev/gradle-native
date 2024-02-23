#include "greeter.h"

#include <string>

std::string say_hello(std::string name) {
#ifdef WITH_FEATURE
#pragma message("compiling with feature enabled")
	return "Hello, " + name + "!";
#else
	return "Bonjour, " + name + "!";
#endif
}
