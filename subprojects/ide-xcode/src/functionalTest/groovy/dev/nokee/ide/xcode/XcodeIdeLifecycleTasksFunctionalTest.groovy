package dev.nokee.ide.xcode

import org.gradle.internal.logging.ConsoleRenderer

class XcodeIdeLifecycleTasksFunctionalTest extends AbstractXcodeIdeFunctionalSpec {
	def "lifecycle task generate the project's ide files only"() {
		given:
		settingsFile << "include 'bar'"
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar')

		when:
		succeeds(':bar:xcode')
		then:
		result.assertTasksExecutedAndNotSkipped(':bar:barXcodeProject', ':bar:xcode')

		when:
		succeeds(':xcode')
		then:
		result.assertTasksExecutedAndNotSkipped(':bar:barXcodeProject', ':fooXcodeProject', ':xcodeWorkspace', ':xcode')
	}

	def "shows message where to find generated workspace only from the root lifecycle task"() {
		given:
		settingsFile << """
			rootProject.name = 'root'
			include 'bar'
		"""
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar')

		when:
		succeeds(':bar:xcode')
		then:
		result.assertNotOutput("Generated Xcode workspace at ${new ConsoleRenderer().asClickableFileUrl(file('root.xcworkspace'))}")

		when:
		succeeds(':xcode')
		then:
		result.assertOutputContains("Generated Xcode workspace at ${new ConsoleRenderer().asClickableFileUrl(file('root.xcworkspace'))}")
	}

	// TODO: Remove stale generated Xcode IDE files
}
