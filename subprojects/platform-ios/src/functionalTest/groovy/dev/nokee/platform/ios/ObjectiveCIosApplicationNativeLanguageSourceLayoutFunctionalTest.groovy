package dev.nokee.platform.ios

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.ios.fixtures.IosTaskNames
import dev.nokee.platform.ios.fixtures.ObjectiveCIosApp
import org.junit.Assume
import spock.lang.Requires

@Requires({ os.macOs })
class ObjectiveCIosApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements ObjectiveCTaskNames, IosTaskNames {
	@Override
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCIosApp()
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << '''
			rootProject.name = 'application'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-ios-application'
			}
		'''
	}

	@Override
	protected void makeProjectWithLibrary() {
		Assume.assumeTrue(false)
	}
}
