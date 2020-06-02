package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec

abstract class AbstractNativeLanguageIncrementalCompilationFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "skip assemble tasks when no source"() {
		given:
		makeSingleProject()

		expect:
		succeeds "assemble"
		result.assertTasksExecuted(tasks.allToAssemble, ":assemble")
		result.assertTasksSkipped(tasks.allToAssemble, ":assemble")
	}

	def "skip objects tasks when no source"() {
		given:
		makeSingleProject()

		expect:
		succeeds "objects"
		result.assertTasksExecuted(tasks.allToObjects, ":objects")
		result.assertTasksSkipped(tasks.allToObjects, ":objects")
	}

	protected abstract void makeSingleProject()

	protected abstract String getBinaryLifecycleTaskName()
}
