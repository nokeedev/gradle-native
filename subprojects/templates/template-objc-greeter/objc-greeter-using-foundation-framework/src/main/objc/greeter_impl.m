#import "greeter.h"

#include <stdlib.h>
#include <string.h>
#include <objc/runtime.h>
#include <Foundation/NSString.h>

@implementation Greeter

+(id)new {
    return class_createInstance(self, 0);
}

- (char *)sayHello:(const char *)name {
    NSString * result_nsstring = [NSString stringWithFormat:@"Bonjour, %s!", name];
    char *result = calloc([result_nsstring length]+1, 1);
	strncpy(result, [result_nsstring UTF8String], [result_nsstring length]);
    return result;
}
@end
