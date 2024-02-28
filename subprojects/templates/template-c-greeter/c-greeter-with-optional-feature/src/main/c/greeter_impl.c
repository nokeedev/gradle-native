#include "greeter.h"

#include <stdlib.h>
#include <string.h>

char * say_hello(const char * name) {
#ifdef WITH_FEATURE
#pragma message("compiling with feature enabled")
	static const char HELLO_STRING[] = "Hello, ";
#else
	static const char HELLO_STRING[] = "Bonjour, ";
#endif
	static const char PONCTUATION_STRING[] = "!";
	char * result = malloc((sizeof(HELLO_STRING)/sizeof(HELLO_STRING[0])) + strlen(name) + (sizeof(PONCTUATION_STRING)/sizeof(PONCTUATION_STRING[0])) + 1); // +1 for the null-terminator
	// TODO: Check for error code from malloc
	// TODO: Initialize result buffer to zeros
	strcpy(result, HELLO_STRING);
	strcat(result, name);
	strcat(result, PONCTUATION_STRING);
	return result;
}
