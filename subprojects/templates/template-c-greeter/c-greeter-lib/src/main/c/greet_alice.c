#include "greet_alice.h"

#include <stdio.h>
#include "greeter.h"

void say_hello_to_alice() {
	printf("%s\n", say_hello("Alice"));
}
