package dev.nokee.language.objectivecpp

import dev.nokee.language.NativeLanguageTaskNames

trait ObjectiveCppTaskNames implements NativeLanguageTaskNames {
	@Override
	String getLanguageTaskSuffix() {
		return 'ObjectiveCpp'
	}

	@Override
	String getSoftwareModelLanguageTaskSuffix() {
		return 'Objcpp'
	}
}
