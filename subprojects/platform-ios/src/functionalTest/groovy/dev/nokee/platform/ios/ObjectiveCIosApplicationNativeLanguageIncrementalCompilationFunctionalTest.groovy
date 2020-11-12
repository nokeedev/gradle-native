package dev.nokee.platform.ios

import dev.nokee.fixtures.AbstractNativeLanguageIncrementalCompilationFunctionalTest
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.ios.fixtures.IosTaskNames
import spock.lang.Requires

@Requires({ os.macOs })
class ObjectiveCIosApplicationNativeLanguageIncrementalCompilationFunctionalTest extends AbstractNativeLanguageIncrementalCompilationFunctionalTest implements ObjectiveCTaskNames, IosTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-ios-application'
			}
		'''
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'bundle'
	}
}
