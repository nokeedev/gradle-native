package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement

class CppCompileGreeter extends NativeSourceElement {
	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('cpp', 'greeter.cpp', '''
#ifdef SAY_HELLO_EVA
#pragma message("Bonjour, Eva!")
#endif
'''))
	}
}
