package dev.nokee.platform.objectivecpp

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeApplicationComponentFunctionalTest
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterApp

class ObjectiveCppApplicationComponentFunctionalTest extends AbstractNativeApplicationComponentFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-application'
			}

			tasks.withType(LinkExecutable).configureEach {
				linkerArgs.add('-lobjc')
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCppGreeterApp()
	}
}
