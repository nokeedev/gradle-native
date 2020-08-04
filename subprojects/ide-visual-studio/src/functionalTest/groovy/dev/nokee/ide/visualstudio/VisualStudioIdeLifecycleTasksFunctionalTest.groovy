package dev.nokee.ide.visualstudio

import dev.nokee.ide.fixtures.AbstractIdeLifecycleTasksFunctionalTest
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeSolutionFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeTaskNames

class VisualStudioIdeLifecycleTasksFunctionalTest extends AbstractIdeLifecycleTasksFunctionalTest implements VisualStudioIdeTaskNames, VisualStudioIdeFixture {
	def "warns users they will need to force a solution rescan"() {
		given:
		settingsFile << '''
			rootProject.name = 'root'
		'''
		buildFile << applyVisualStudioIdePlugin() << configureVisualStudioIdeProject('foo')

		and:
		run('visualStudio').assertTasksExecutedAndNotSkipped(tasks.allToIde('foo'))

		and: 'simulate lock'
		def lock = visualStudioSolution('root').dotvsDirectory.simulateVisualStudioIdeLock()

		when:
		succeeds('visualStudio')

		then:
		result.assertOutputContains('''
			|============
			|Visual Studio is currently holding the solution 'root.sln' open.
			|This may impact features such as code navigation and code editing.
			|We recommend manually triggering a solution rescan from the Visual Studio via Project > Rescan Solution.
			|In the future, try closing your solution before executing the visualStudio task.
			|To learn more about this issue, visit https://docs.nokee.dev/intellisense-reconcilation
			|============'''.stripMargin())

		cleanup:
		lock?.close()
	}

	@Override
	protected String getIdeWorkspaceDisplayNameUnderTest() {
		return 'Visual Studio solution'
	}

	@Override
	protected String workspaceName(String name) {
		return VisualStudioIdeSolutionFixture.solutionName(name)
	}

	@Override
	protected String getIdeUnderTestDsl() {
		return 'visualStudio'
	}

	@Override
	protected String configureIdeProject(String name) {
		return configureVisualStudioIdeProject(name)
	}

	@Override
	protected String getIdePluginId() {
		return visualStudioIdePluginId
	}
}
