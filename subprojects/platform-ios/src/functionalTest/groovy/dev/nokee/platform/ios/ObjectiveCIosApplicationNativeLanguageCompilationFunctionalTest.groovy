package dev.nokee.platform.ios

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeLanguageCompilationFunctionalTest
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.ios.fixtures.IosTaskNames
import dev.nokee.platform.ios.fixtures.ObjectiveCIosApp
import spock.lang.Requires

@Requires({ os.macOs })
class ObjectiveCIosApplicationNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements ObjectiveCTaskNames, IosTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-ios-application'
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCIosApp()
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'bundle'
	}

	@Override
	protected boolean isTargetMachineAwareConfiguration() {
		return false
	}
}
