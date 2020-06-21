package dev.nokee.platform.ios

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeLanguageCompilationFunctionalTest
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.ios.fixtures.IosTaskNames
import dev.nokee.platform.ios.fixtures.ObjectiveCIosApp
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

@Requires({ SystemUtils.IS_OS_MAC})
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
	protected String getExpectedCompilationFailureCause() {
		return 'Objective-C compiler failed while compiling broken.m'
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
