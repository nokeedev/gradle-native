package dev.nokee.language.objectivec

import dev.nokee.language.NativeLanguageTaskNames
import dev.nokee.language.NativeProjectTaskNames

trait ObjectiveCTaskNames implements NativeLanguageTaskNames, NativeProjectTaskNames {
	@Override
	String getLanguageTaskSuffix() {
		return 'ObjectiveC'
	}

	@Override
	String getSoftwareModelLanguageTaskSuffix() {
		return 'Objc'
	}
}
