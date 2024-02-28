#include "greeter.h"

#include <stdlib.h>
#include <string.h>

char * say_hello(const char * name) {
	static const char HELLO_STRING[] = "Bonjour, ";
	static const char PONCTUATION_STRING[] = "!";
	char * result = malloc((sizeof(HELLO_STRING)/sizeof(HELLO_STRING[0])) + strlen(name) + (sizeof(PONCTUATION_STRING)/sizeof(PONCTUATION_STRING[0])) + 1); // +1 for the null-terminator
	// TODO: Check for error code from malloc
	// TODO: Initialize result buffer to zeros
	strcpy(result, HELLO_STRING);
	strcat(result, name);
	strcat(result, PONCTUATION_STRING);
	return result;
}
