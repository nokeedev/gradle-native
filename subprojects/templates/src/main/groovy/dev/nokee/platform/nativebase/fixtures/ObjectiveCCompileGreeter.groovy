package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile

class ObjectiveCCompileGreeter extends SourceElement {
	@Override
	List<SourceFile> getFiles() {
		return [sourceFile('objc', 'greeter.m', '''
#ifdef SAY_HELLO_EVA
#pragma message("Bonjour, Eva!")
#endif
''')]
	}
}
