#import "greeter.h"

#include <string>
#include <objc/runtime.h>

@implementation Greeter

+(id)new {
    return class_createInstance(self, 0);
}

- (std::string)sayHello:(std::string)name {
    return "Bonjour, " + name + "!";
}
@end
