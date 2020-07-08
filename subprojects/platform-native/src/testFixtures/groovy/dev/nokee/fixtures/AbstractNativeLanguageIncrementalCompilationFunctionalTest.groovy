package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.NativeProjectTasks

abstract class AbstractNativeLanguageIncrementalCompilationFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "skip assemble tasks when no source"() {
		given:
		makeSingleProject()

		expect:
		succeeds "assemble"
		result.assertTasksExecuted(taskNamesUnderTest.allToLifecycleAssemble)
		result.assertTasksSkipped(allSkippedTasksToAssemble)
	}

	def "skip objects tasks when no source"() {
		given:
		makeSingleProject()

		expect:
		succeeds "objects"
		result.assertTasksExecuted(taskNamesUnderTest.allToLifecycleObjects)
		result.assertTasksSkipped(taskNamesUnderTest.allToLifecycleObjects)
	}

	protected abstract void makeSingleProject()

	protected abstract String getBinaryLifecycleTaskName()

	protected List<String> getAllSkippedTasksToAssemble() {
		return taskNamesUnderTest.allToLifecycleAssemble
	}

	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks
	}
}
