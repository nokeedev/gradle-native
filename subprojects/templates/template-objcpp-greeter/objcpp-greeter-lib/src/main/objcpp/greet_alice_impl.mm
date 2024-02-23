#include "greet_alice.h"

#include <iostream>
#include <objc/runtime.h>

#import "greeter.h"

@implementation GreetAlice

+(id)alloc {
    return class_createInstance(self, 0);
}

- (void)sayHelloToAlice {
	Greeter* greeter = [Greeter new];
	std::cout << [greeter sayHello:"Alice"] << std::endl;
}
@end
