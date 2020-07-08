package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.NativeProjectTasks
import spock.lang.Unroll

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
		succeeds ":assemble"
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
		succeeds ":assemble"
		result.assertTasksExecuted(taskNamesUnderTest.allToLifecycleAssemble)

		// TODO: Improve this assertion
		file("build/objs/main").assertIsDirectory()
	}

	@Unroll
	def "can depends on library with custom source layout"(linkages) {
		given:
		makeComponentWithLibrary()
		file('library', buildFileName) << """
			library {
				targetLinkages = ${linkages}
			}
		"""

		expect:
		succeeds ':assemble'
		result.assertTasksExecuted(taskNamesUnderTest.allToLifecycleAssemble, allTasksToLinkLibrary)

		where:
		linkages << ['null', '[linkages.static]', '[linkages.shared]', '[linkages.static, linkages.shared]']

	}

	protected List<String> getAllTasksToLinkLibrary() {
		def libraryBuildFile = file('library', buildFileName)

		def libraryTasks = tasks(':library')
		if (libraryBuildFile.text.contains('[linkages.static, linkages.shared]')) {
			libraryTasks = libraryTasks.withLinkage('shared')
		}

		def result = libraryTasks.allToLink
		if (libraryBuildFile.text.contains('[linkages.static]')) {
			result = libraryTasks.allToCreate
		}

		if (this.class.name.contains('WithStaticLinkage')) {
			if (this.class.simpleName.startsWith('Swift')) {
				result = [libraryTasks.compile]
			} else {
				result = []
			}
		}

		return result
	}

	protected abstract void makeSingleComponent()

	protected abstract void makeComponentWithLibrary()

	protected abstract String configureSourcesAsConvention()

	protected abstract String configureSourcesAsExplicitFiles()

	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks
	}
}
