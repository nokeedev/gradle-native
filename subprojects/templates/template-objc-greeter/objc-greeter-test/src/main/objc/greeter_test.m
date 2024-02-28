#include <objc/runtime.h>

#include "greeter_fixtures.h"
#include "greeter.h"

int main(int argc, const char ** argv) {
	Greeter* greeter = [Greeter new];
	if ([greeter sayHello:"Alice"] == "Bonjour, Alice!") {
		return PASS;
	}
	return FAIL;
}
