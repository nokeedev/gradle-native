package dev.nokee.platform.objectivec

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeApplicationComponentFunctionalTest
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterApp

class ObjectiveCApplicationComponentFunctionalTest extends AbstractNativeApplicationComponentFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-c-application'
			}

			tasks.withType(LinkExecutable).configureEach {
				linkerArgs.add('-lobjc')
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCGreeterApp()
	}
}
