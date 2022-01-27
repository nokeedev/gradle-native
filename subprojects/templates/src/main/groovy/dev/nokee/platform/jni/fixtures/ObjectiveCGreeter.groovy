package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.SourceElement

import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile

class ObjectiveCGreeter extends NativeLibraryElement {
	private final header
	private final source

	@Override
    SourceElement getPublicHeaders() {
		return header
	}

	@Override
	SourceElement getSources() {
		return source
	}

	ObjectiveCGreeter() {
		header = ofFile(sourceFile('headers', 'greeter.h', """
#ifdef __has_attribute
#if __has_attribute(objc_root_class)
__attribute__((objc_root_class))
#endif
#endif
@interface Greeter { id isa; }
+ (id)new;
- (char *)sayHello:(const char *)name;
@end
"""))
		source = ofFile(sourceFile('objc', 'greeter_impl.m', """
#import "greeter.h"

#include <stdlib.h>
#include <string.h>
#include <objc/runtime.h>

@implementation Greeter

+(id)new {
    return class_createInstance(self, 0);
}

- (char *)sayHello:(const char *)name {
    static const char HELLO_STRING[] = "Bonjour, ";
    static const char PONCTUATION_STRING[] = "!";
    char * result = malloc((sizeof(HELLO_STRING)/sizeof(HELLO_STRING[0])) + strlen(name) + (sizeof(PONCTUATION_STRING)/sizeof(PONCTUATION_STRING[0])) + 1); // +1 for the null-terminator
    // TODO: Check for error code from malloc
    // TODO: Initialize result buffer to zeros
    strcpy(result, HELLO_STRING);
    strcat(result, name);
    strcat(result, PONCTUATION_STRING);
    return result;
}
@end
"""))
	}

	NativeLibraryElement withFoundationFrameworkImplementation() {
		return new NativeLibraryElement() {
			@Override
			SourceElement getPublicHeaders() {
				return header
			}

			@Override
			SourceElement getSources() {
				return ofFile(sourceFile('objc', 'greeter_impl.m', """
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
"""))
			}
		}
	}

	NativeLibraryElement withOptionalFeature() {
		return new NativeLibraryElement() {
			@Override
			SourceElement getPublicHeaders() {
				return header
			}

			@Override
			SourceElement getSources() {
				return ofFile(sourceFile('objc', 'greeter_impl.m', """
#import "greeter.h"

#include <stdlib.h>
#include <string.h>
#include <objc/runtime.h>

@implementation Greeter

+(id)new {
    return class_createInstance(self, 0);
}

- (char *)sayHello:(const char *)name {
#ifdef WITH_FEATURE
#pragma message("compiling with feature enabled")
	static const char HELLO_STRING[] = "Hello, ";
#else
	static const char HELLO_STRING[] = "Bonjour, ";
#endif
    static const char PONCTUATION_STRING[] = "!";
    char * result = malloc((sizeof(HELLO_STRING)/sizeof(HELLO_STRING[0])) + strlen(name) + (sizeof(PONCTUATION_STRING)/sizeof(PONCTUATION_STRING[0])) + 1); // +1 for the null-terminator
    // TODO: Check for error code from malloc
    // TODO: Initialize result buffer to zeros
    strcpy(result, HELLO_STRING);
    strcat(result, name);
    strcat(result, PONCTUATION_STRING);
    return result;
}
@end
"""))
			}
		}
	}
}
