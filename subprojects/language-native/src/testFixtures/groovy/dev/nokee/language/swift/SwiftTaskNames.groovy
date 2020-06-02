package dev.nokee.language.swift

import dev.nokee.language.NativeLanguageTaskNames

trait SwiftTaskNames implements NativeLanguageTaskNames {
	@Override
	String getLanguageTaskSuffix() {
		return 'Swift'
	}
}
