package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile
import dev.gradleplugins.fixtures.sources.SourceFileElement
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter

class SwiftGreeterApp extends GreeterImplementationAwareSourceElement<SwiftGreeter> {
	@Delegate
	final SourceElement delegate

	SwiftGreeterApp() {
		super(new SwiftMainUsesGreeter(), new SwiftGreeter())
		delegate = ofElements(elementUsingGreeter, greeter)
	}

	@Override
	SwiftMainUsesGreeter getElementUsingGreeter() {
		return (SwiftMainUsesGreeter) super.getElementUsingGreeter()
	}

	GreeterImplementationAwareSourceElement<SourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter.withImport(subprojectPath.capitalize()), asSubproject(subprojectPath, greeter))
	}

	private static class SwiftMainUsesGreeter extends SourceFileElement {
		@Override
		SourceFile getSourceFile() {
			return sourceFile('swift', 'main.swift', '''
func main() -> Int {
	let greeter = Greeter()
	print(greeter.sayHello(name: "Alice"))
	return 0
}

_ = main()
''')
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
}
