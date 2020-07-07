package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.NativeProjectTasks

abstract class AbstractNativeLanguageSourceLayoutFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {

	def "can change source layout convention"() {
		given:
		makeSingleComponent()
		buildFile << configureSourcesAsConvention()

		and:
		file("src/main/c/broken.c") << "broken!"
		file("src/main/cpp/broken.cpp") << "broken!"
		file("src/main/objc/broken.m") << "broken!"
		file("src/main/objcpp/broken.mm") << "broken!"
		file("src/main/swift/broken.swift") << "broken!"

		expect:
		succeeds "assemble"
		result.assertTasksExecuted(taskNamesUnderTest.allToLifecycleAssemble)

		// TODO: Improve this assertion
		file("build/objs/main").assertIsDirectory()
	}

	def "can add individual source files"() {
		given:
		makeSingleComponent()
		buildFile << configureSourcesAsExplicitFiles()

		and:
		file("src/main/c/broken.c") << "broken!"
		file("src/main/cpp/broken.cpp") << "broken!"
		file("src/main/objc/broken.m") << "broken!"
		file("src/main/objcpp/broken.mm") << "broken!"
		file("src/main/swift/broken.swift") << "broken!"

		expect:
		succeeds "assemble"
		result.assertTasksExecuted(taskNamesUnderTest.allToLifecycleAssemble)

		// TODO: Improve this assertion
		file("build/objs/main").assertIsDirectory()
	}

	protected abstract void makeSingleComponent()

	protected abstract String configureSourcesAsConvention()

	protected abstract String configureSourcesAsExplicitFiles()

	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks
	}
}
