#include "greet_alice.h"

#include <iostream>
#include "greeter.h"

void say_hello_to_alice() {
	std::cout << say_hello("Alice") << std::endl;
}
