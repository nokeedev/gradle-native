package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter

class SwiftGreeterApp extends SourceElement {
	@Override
	List<SourceFile> getFiles() {
		return [new SwiftMainUsesGreeter().sourceFile, new SwiftGreeter().sourceFile]
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
