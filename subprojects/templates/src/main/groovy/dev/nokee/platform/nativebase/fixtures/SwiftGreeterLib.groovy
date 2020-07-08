package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter

class SwiftGreeterLib extends GreeterImplementationAwareSourceElement<SwiftGreeter> {
	@Delegate final SourceElement delegate

	SwiftGreeterLib() {
		super(new SwiftGreetUsesGreeter(), new SwiftGreeter())
		delegate = ofElements(elementUsingGreeter, greeter)
	}

	@Override
	SwiftGreetUsesGreeter getElementUsingGreeter() {
		return (SwiftGreetUsesGreeter) super.getElementUsingGreeter()
	}

	GreeterImplementationAwareSourceElement<SourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter.withImport(subprojectPath.capitalize()), asSubproject(subprojectPath, greeter))
	}
}

class SwiftGreetUsesGreeter extends SourceFileElement {
	@Override
	SourceFile getSourceFile() {
		return sourceFile('swift', 'greeter.swift', """
public class GreetAlice {
	public init() {}
	public func sayHelloToAlice() {
		let greeter = Greeter()
		print(greeter.sayHello(name: "Alice"))
	}
}
""")
	}

	SourceFileElement withImport(String moduleToImport) {
		def delegate = sourceFile
		return new SourceFileElement() {
			@Override
			SourceFile getSourceFile() {
				return sourceFile(delegate.path, delegate.name, """
import ${moduleToImport}

${delegate.content}
""")
			}
		}
	}
}
