package dev.nokee.language.objectivec

import dev.nokee.language.NativeLanguageTaskNames

trait ObjectiveCTaskNames implements NativeLanguageTaskNames {
	@Override
	String getLanguageTaskSuffix() {
		return 'ObjectiveC'
	}

	@Override
	String getSoftwareModelLanguageTaskSuffix() {
		return 'Objc'
	}
}
