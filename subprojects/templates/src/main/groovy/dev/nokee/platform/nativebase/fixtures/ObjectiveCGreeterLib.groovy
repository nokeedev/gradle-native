package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile

class ObjectiveCGreeterLib extends GreeterImplementationAwareSourceElement<ObjectiveCGreeter> {
	@Delegate
	final NativeLibraryElement delegate

	ObjectiveCGreeterLib() {
		super(new ObjectiveCGreetUsesGreeter().asLib(), new ObjectiveCGreeter().asLib())
		delegate = ofNativeLibraryElements(elementUsingGreeter, greeter)
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, greeter))
	}

	private static class ObjectiveCGreetUsesGreeter extends NativeLibraryElement {
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

		ObjectiveCGreetUsesGreeter() {
			header = ofFile(sourceFile('headers', 'greet_alice.h', """
#ifdef __has_attribute
#if __has_attribute(objc_root_class)
__attribute__((objc_root_class))
#endif
#endif
@interface GreetAlice { id isa; }
+ (id)new;
- (void)sayHelloToAlice;
@end
"""))
			source = ofFile(sourceFile('objc', 'greet_alice_impl.m', """
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
	printf("%s\\n", [greeter sayHello:"Alice"]);
}
@end
"""))
		}
	}
}
