package dev.nokee.ide.visualstudio

import dev.nokee.ide.fixtures.AbstractIdeCleanTaskFunctionalTest
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeTaskNames

import static dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture.filtersName
import static dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture.projectName
import static dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeSolutionFixture.solutionName

class VisualStudioIdeCleanTaskFunctionalTest extends AbstractIdeCleanTaskFunctionalTest implements VisualStudioIdeTaskNames, VisualStudioIdeFixture {
	@Override
	protected String getIdePluginId() {
		return visualStudioIdePluginId
	}

	@Override
	protected String configureIdeProject(String name) {
		return configureVisualStudioIdeProject(name)
	}

	@Override
	protected IdeFiles ideProjectFiles(String path) {
		return ofFiles(projectName(path), filtersName(path))
	}

	@Override
	protected IdeFiles ideWorkspaceFiles(String path) {
		return ofFiles(solutionName(path))
	}

	def "cleans Visual Studio IDE user files when present"() {
		given:
		buildFile << applyIdePlugin() << configureIdeProject('foo')

		and:
		run(tasks.ideLifecycle)

		and:
		def userProjectFile = file('foo.vcxproj.user').createFile()

		when:
		succeeds(tasks.ideClean)

		then:
		userProjectFile.assertDoesNotExist()
	}

	def "cleans .vs directory when present"() {
		given:
		settingsFile << "rootProject.name = 'foo'"
		buildFile << applyIdePlugin() << configureIdeProject('foo')

		and:
		run(tasks.ideLifecycle)

		and:
		def dotvsDirectory = visualStudioSolution('foo').dotvsDirectory

		when:
		succeeds(tasks.ideClean)

		then:
		dotvsDirectory.assertDoesNotExist()
	}

	def "warns what to do when any files inside .vs directory is locked"() {
		given:
		settingsFile << "rootProject.name = 'foo'"
		buildFile << applyIdePlugin() << configureIdeProject('foo')

		and:
		run(tasks.ideLifecycle)

		and:
		def dotvsDirectory = visualStudioSolution('foo').dotvsDirectory
		def lock = dotvsDirectory.simulateVisualStudioIdeLock()

		when:
		def failure = fails(tasks.ideClean)

		then:
		failure.assertHasDescription("Execution failed for task ':cleanVisualStudio'.")
		failure.assertHasCause("Please close your Visual Studio IDE before executing 'cleanVisualStudio'.")

		and:
		dotvsDirectory.assertExists()

		cleanup:
		lock?.close()
	}
}
