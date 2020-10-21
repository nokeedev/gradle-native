package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.ObjectiveCppGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile

class ObjectiveCppGreeterLib extends GreeterImplementationAwareSourceElement<ObjectiveCppGreeter> {
	@Delegate
	final NativeLibraryElement delegate

	ObjectiveCppGreeterLib() {
		super(new ObjectiveCppGreetUsesGreeter().asLib(), new ObjectiveCppGreeter().asLib())
		delegate = ofNativeLibraryElements(elementUsingGreeter, greeter)
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, greeter))
	}

	private static class ObjectiveCppGreetUsesGreeter extends NativeLibraryElement {
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

		ObjectiveCppGreetUsesGreeter() {
			header = ofFile(sourceFile('headers', 'greet_alice.h', """
@interface GreetAlice
+ (id)alloc;
- (void)sayHelloToAlice;
@end
"""))
			source = ofFile(sourceFile('objcpp', 'greet_alice_impl.mm', """
#include "greet_alice.h"

#include <iostream>
#include <objc/runtime.h>

#import "greeter.h"

@implementation GreetAlice

+(id)alloc {
    return class_createInstance(self, 0);
}

- (void)sayHelloToAlice {
	Greeter* greeter = [Greeter alloc];
	std::cout << [greeter sayHello:"Alice"] << std::endl;
}
@end
"""))
		}
	}
}
