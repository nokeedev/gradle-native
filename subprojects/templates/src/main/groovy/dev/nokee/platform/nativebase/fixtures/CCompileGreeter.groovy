package dev.nokee.platform.nativebase.fixtures

import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement

class CCompileGreeter extends NativeSourceElement {
	@Override
	SourceElement getSources() {
		return ofFiles(sourceFile('c', 'greeter.c', '''
#ifdef SAY_HELLO_EVA
#pragma message("Bonjour, Eva!")
#endif
'''))
	}
}
