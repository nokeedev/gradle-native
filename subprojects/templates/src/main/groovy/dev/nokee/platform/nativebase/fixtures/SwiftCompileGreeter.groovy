package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile

class SwiftCompileGreeter extends SourceElement {
	@Override
	List<SourceFile> getFiles() {
		return [sourceFile('swift', 'greeter.swift', '''
#if SAY_HELLO_EVA
#warning("Bonjour, Eva!")
#endif
''')]
	}
}
