package dev.nokee.language.cpp

import dev.nokee.language.NativeLanguageTaskNames

trait CppTaskNames implements NativeLanguageTaskNames {
	@Override
	String getLanguageTaskSuffix() {
		return 'Cpp'
	}
}
