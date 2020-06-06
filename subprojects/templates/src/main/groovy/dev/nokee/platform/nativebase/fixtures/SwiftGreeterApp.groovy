package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter

class SwiftGreeterApp extends SourceElement {
	private final main
	private final greeter

	SwiftGreeterApp() {
		this(new SwiftMainUsesGreeter(), new SwiftGreeter())
	}

	SwiftGreeterApp(SourceElement main, SourceElement greeter) {
		this.main = main
		this.greeter = greeter
	}

	@Override
	List<SourceFile> getFiles() {
		return main.files + greeter.files
	}

	SourceElement withImplementationAsSubproject(String subprojectName) {
		return new SourceElement() {
			@Override
			List<SourceFile> getFiles() {
				throw new UnsupportedOperationException()
			}

			@Override
			void writeToProject(TestFile projectDir) {
				ofFiles(sourceFile(main.sourceFile.path, main.sourceFile.name, "import ${subprojectName.capitalize()}\n\n${main.sourceFile.content}")).writeToProject(projectDir)
				greeter.writeToProject(projectDir.file(subprojectName))
			}
		}
	}
}

class SwiftMainUsesGreeter extends SourceFileElement {
	@Override
	SourceFile getSourceFile() {
		return sourceFile('swift', 'main.swift', '''
func main() -> Int {
	let greeter = Greeter()
	greeter.sayHello(name: "Alice")
	return 0
}

_ = main()
''')
	}
}
