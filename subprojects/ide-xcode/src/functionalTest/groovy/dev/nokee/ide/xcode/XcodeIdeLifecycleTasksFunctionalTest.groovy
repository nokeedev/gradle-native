package dev.nokee.ide.xcode

import org.gradle.internal.logging.ConsoleRenderer

class XcodeIdeLifecycleTasksFunctionalTest extends AbstractXcodeIdeFunctionalSpec {
	def "can clean generated Xcode files"() {
		given:
		settingsFile << '''
			rootProject.name = 'foo'
			include 'bar', 'far'
		'''
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar')
		file('far/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('far')

		when:
		succeeds('xcode')
		then:
		file('foo.xcworkspace').assertExists()
		file('foo.xcodeproj').assertExists()
		file('bar/bar.xcodeproj').assertExists()
		file('far/far.xcodeproj').assertExists()

		when:
		succeeds(':far:cleanXcode')
		then:
		file('far/far.xcodeproj').assertDoesNotExist()
		and:
		file('foo.xcworkspace').assertExists()
		file('foo.xcodeproj').assertExists()
		file('bar/bar.xcodeproj').assertExists()

		when:
		succeeds('cleanXcode')
		then:
		file('far/far.xcodeproj').assertDoesNotExist()
		and:
		file('foo.xcworkspace').assertDoesNotExist()
		file('foo.xcodeproj').assertDoesNotExist()
		file('bar/bar.xcodeproj').assertDoesNotExist()
	}

	def "does not clean generated Xcode files using project clean lifecycle task"() {
		given:
		settingsFile << "rootProject.name = 'foo'"
		buildFile << applyXcodeIdePlugin() << "apply plugin: 'lifecycle-base'" << configureXcodeIdeProject('foo')

		when:
		succeeds('xcode')
		then:
		file('foo.xcworkspace').assertExists()
		file('foo.xcodeproj').assertExists()

		when:
		succeeds('clean')
		then:
		file('foo.xcworkspace').assertExists()
		file('foo.xcodeproj').assertExists()
	}

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
