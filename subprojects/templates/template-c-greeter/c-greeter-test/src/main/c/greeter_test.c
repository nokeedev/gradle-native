#include "greeter_fixtures.h"
#include <string.h>
#include <stdlib.h>
#include "greeter.h"

int main(int argc, char** argv) {
	char * value = say_hello("Alice");
	if (0 == strcmp(value, "Bonjour, Alice!")) {
		free(value);
		return PASS;
	}
	free(value);
	return FAIL;
}
