package dev.nokee.platform.nativebase.fixtures


import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.gradleplugins.test.fixtures.sources.SourceFileElement

class SwiftGreeterTest extends SourceFileElement {
	private final String testedModuleName

	SwiftGreeterTest(String testedModuleName) {
		this.testedModuleName = testedModuleName
	}

	@Override
	String getSourceSetName() {
		return 'test'
	}

	@Override
	SourceFile getSourceFile() {
		return sourceFile('swift', 'greeter_test.swift', """
import ${testedModuleName}

func main() -> Int {
	let greeter = Greeter()
	if (greeter.sayHello(name: "Alice") == "Bonjour, Alice!") {
		return 0
	}
	return -1
}

_ = main()
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
