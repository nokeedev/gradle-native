package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec

abstract class AbstractNativeLanguageIncrementalCompilationFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "skip assemble tasks when no source"() {
		given:
		makeSingleProject()

		expect:
		succeeds "assemble"
		result.assertTasksExecuted(tasks.allToAssemble)
		result.assertTasksSkipped(allSkippedTasksToAssemble)
	}

	def "skip objects tasks when no source"() {
		given:
		makeSingleProject()

		expect:
		succeeds "objects"
		result.assertTasksExecuted(tasks.allToObjects)
		result.assertTasksSkipped(tasks.allToObjects)
	}

	protected abstract void makeSingleProject()

	protected abstract String getBinaryLifecycleTaskName()

	protected List<String> getAllSkippedTasksToAssemble() {
		return tasks.allToAssemble
	}
}
