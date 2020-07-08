package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeLibraryElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.objectivec.ObjectiveCLibraryElement
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class ObjectiveCGreeterLib extends GreeterImplementationAwareSourceElement<ObjectiveCGreeter> {
	@Delegate final NativeLibraryElement delegate

	ObjectiveCGreeterLib() {
		super(new ObjectiveCGreetUsesGreeter(), new ObjectiveCGreeter())
		delegate = ofNativeLibraryElements(elementUsingGreeter, greeter)
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, greeter.asLib()))
	}
}

class ObjectiveCGreetUsesGreeter extends ObjectiveCLibraryElement {
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
@interface GreetAlice
+ (id)alloc;
- (void)sayHelloToAlice;
@end
"""))
		source = ofFile(sourceFile('objc', 'greet_alice_impl.m', """
#import "greet_alice.h"

#include <stdio.h>
#include <objc/runtime.h>

#include "greeter.h"

@implementation GreetAlice

+(id)alloc {
    return class_createInstance(self, 0);
}

- (void)sayHelloToAlice {
	Greeter* greeter = [Greeter alloc];
	printf("%s\\n", [greeter sayHello:"Alice"]);
}
@end
"""))
	}
}
