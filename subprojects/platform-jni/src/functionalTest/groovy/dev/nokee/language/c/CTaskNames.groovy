package dev.nokee.language.c

import dev.nokee.language.NativeLanguageTaskNames

trait CTaskNames implements NativeLanguageTaskNames {
	@Override
	String getLanguageTaskSuffix() {
		return 'C'
	}
}
