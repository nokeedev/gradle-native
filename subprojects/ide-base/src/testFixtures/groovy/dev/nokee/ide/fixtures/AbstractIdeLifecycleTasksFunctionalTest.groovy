package dev.nokee.ide.fixtures

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import org.gradle.internal.logging.ConsoleRenderer

abstract class AbstractIdeLifecycleTasksFunctionalTest extends AbstractGradleSpecification {
	def "generates the project's IDE files only"() {
		given:
		settingsFile << "include 'bar'"
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar1') << configureIdeProject('bar2')

		when:
		succeeds(tasks(':bar').ideLifecycle)
		then:
		result.assertTasksExecutedAndNotSkipped(tasks(':bar').allToIde('bar1', 'bar2'))
	}

	def "generates the root project IDE files even when not part of the IDE workspace"() {
		given:
		buildFile << applyIdePlugin() << configureIdeProject('root')

		when:
		succeeds(tasks.ideLifecycle)
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToIde('root'))

		when:
		buildFile << """
			${ideUnderTestDsl} {
				workspace.projects = []
			}
		"""
		succeeds(tasks.ideLifecycle)
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToIde('root'))
	}

	def "shows message where to find generated workspace only from the root lifecycle task"() {
		given:
		settingsFile << """
			rootProject.name = 'root'
			include 'bar'
		"""
		buildFile << applyIdePlugin() << configureIdeProject('foo')
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar')

		when:
		succeeds(tasks(':bar').ideLifecycle)
		then:
		result.assertNotOutput("Generated ${ideWorkspaceDisplayNameUnderTest} at ${new ConsoleRenderer().asClickableFileUrl(file(workspaceName('root')))}")

		when:
		succeeds(tasks.ideLifecycle)
		then:
		result.assertOutputContains("Generated ${ideWorkspaceDisplayNameUnderTest} at ${new ConsoleRenderer().asClickableFileUrl(file(workspaceName('root')))}")
	}

	protected abstract String getIdeWorkspaceDisplayNameUnderTest()

	protected abstract String workspaceName(String name)

	protected abstract String getIdeUnderTestDsl()

	protected abstract String configureIdeProject(String name)

	protected abstract String getIdePluginId()

	protected String applyIdePlugin() {
		return """
			plugins {
				id '${idePluginId}'
			}
		"""
	}
}
