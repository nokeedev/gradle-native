package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.SourceElement

import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile

class ObjectiveCppGreeter extends NativeLibraryElement {
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

	ObjectiveCppGreeter() {
		header = ofFile(sourceFile('headers', 'greeter.h', """
#include <string>

@interface Greeter
+ (id)alloc;
- (std::string)sayHello:(std::string)name;
@end
"""))
		source = ofFile(sourceFile('objcpp', 'greeter_impl.mm', """
#import "greeter.h"

#include <string>
#include <objc/runtime.h>

@implementation Greeter

+(id)alloc {
    return class_createInstance(self, 0);
}

- (std::string)sayHello:(std::string)name {
    return "Bonjour, " + name + "!";
}
@end
"""))
	}

	NativeLibraryElement withOptionalFeature() {
		return new NativeLibraryElement() {
			@Override
			SourceElement getPublicHeaders() {
				return header
			}

			@Override
			SourceElement getSources() {
				return ofFile(sourceFile('objcpp', 'greeter_impl.mm', """
#import "greeter.h"

#include <string>
#include <objc/runtime.h>

@implementation Greeter

+(id)alloc {
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
"""))
			}
		}
	}
}
