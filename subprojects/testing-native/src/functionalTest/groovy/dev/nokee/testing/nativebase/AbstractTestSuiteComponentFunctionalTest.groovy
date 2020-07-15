package dev.nokee.testing.nativebase

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.sources.SourceElement

abstract class AbstractTestSuiteComponentFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can test generic executable-based test suite"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('test')

		then:
		result.assertTasksExecuted(tasks.compile, tasks.withComponentName('test').allToTest)
	}

	def "can execute tests using check task"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('check')

		then:
		result.assertTasksExecuted(tasks.compile, tasks.withComponentName('test').allToCheck)
	}

	// TODO: Test failing test suite
	// TODO: Test generic test suite for Objective-C
	// TODO: Test generic test suite for Objective-C++
	// TODO: Test generic test suite for Swift

	protected abstract SourceElement getComponentUnderTest()

	protected abstract void makeSingleProject()
}


