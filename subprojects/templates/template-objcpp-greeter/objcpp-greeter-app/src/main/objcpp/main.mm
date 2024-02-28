#include <iostream>
#include "greeter.h"

int main(int argc, const char * argv[]) {
	Greeter* greeter = [Greeter new];
	std::cout << [greeter sayHello:"Alice"] << std::endl;
	return 0;
}
