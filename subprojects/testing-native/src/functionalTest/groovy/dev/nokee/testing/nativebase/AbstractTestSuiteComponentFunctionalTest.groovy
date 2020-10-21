package dev.nokee.testing.nativebase

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.language.NativeProjectTasks

abstract class AbstractTestSuiteComponentFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can test generic executable-based test suite"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('test')

		then:
		result.assertTasksExecuted(tasks.compile, tasksUnderTest.allToTest)
	}

	def "can execute tests using check task"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('check')

		then:
		result.assertTasksExecuted(tasks.compile, tasksUnderTest.allToCheck)
	}

	def "can execute tests for each build types"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
			${testedComponentDsl} {
				targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]
			}
		"""

		when:
		succeeds('testRelease')

		then:
		result.assertTasksExecuted(tasks.withBuildType('release').compile, tasksUnderTest.withBuildType('release').allToTest)
	}

	protected NativeProjectTasks getTasksUnderTest() {
		return tasks.withComponentName('test')
	}

	// TODO: Test failing test suite
	// TODO: Test generic test suite for Objective-C
	// TODO: Test generic test suite for Objective-C++
	// TODO: Test generic test suite for Swift

	protected String getTestedComponentDsl() {
		if (this.class.simpleName.contains('Application')) {
			return 'application'
		}
		return 'library'
	}

	protected abstract SourceElement getComponentUnderTest()

	protected abstract void makeSingleProject()
}


