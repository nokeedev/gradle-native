package dev.nokee.platform.ios


import dev.nokee.fixtures.AbstractNativeLanguageIncrementalCompilationFunctionalTest
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.ios.fixtures.IosTaskNames
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

@Requires({ SystemUtils.IS_OS_MAC})
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
