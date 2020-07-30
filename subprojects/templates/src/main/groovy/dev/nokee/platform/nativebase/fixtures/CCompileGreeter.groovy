package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile

class CCompileGreeter extends SourceElement {
	@Override
	List<SourceFile> getFiles() {
		return [sourceFile('c', 'greeter.c', '''
#ifdef SAY_HELLO_EVA
#pragma message("Bonjour, Eva!")
#endif
''')]
	}
}
