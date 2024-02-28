#include "greeter_fixtures.h"
#include "greeter.h"

int main(int argc, char* argv[]) {
	if (say_hello("Alice") == "Bonjour, Alice!") {
		return PASS;
	}
	return FAIL;
}
