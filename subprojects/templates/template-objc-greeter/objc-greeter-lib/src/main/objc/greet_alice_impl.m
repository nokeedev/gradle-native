#import "greet_alice.h"

#include <stdio.h>
#include <objc/runtime.h>

#include "greeter.h"

@implementation GreetAlice

+(id)new {
    return class_createInstance(self, 0);
}

- (void)sayHelloToAlice {
	Greeter* greeter = [Greeter new];
	printf("%s\n", [greeter sayHello:"Alice"]);
}
@end
