#import "greeter.h"

#include <string>
#include <objc/runtime.h>

@implementation Greeter

+(id)new {
    return class_createInstance(self, 0);
}

- (std::string)sayHello:(std::string)name {
#ifdef WITH_FEATURE
#pragma message("compiling with feature enabled")
	return "Hello, " + name + "!";
#else
    return "Bonjour, " + name + "!";
#endif
}
@end
