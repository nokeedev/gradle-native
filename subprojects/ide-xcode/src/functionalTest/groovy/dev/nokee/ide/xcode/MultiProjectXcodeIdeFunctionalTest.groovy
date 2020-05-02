package dev.nokee.ide.xcode

class MultiProjectXcodeIdeFunctionalTest extends AbstractXcodeIdeFunctionalSpec {
	def "includes Xcode projects from subprojects"() {
		given:
		settingsFile << """
			rootProject.name = 'root'
			include 'foo'
			include 'bar'
		"""
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('root')
		file('foo/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('foo1') << configureXcodeIdeProject('foo2')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar1') << configureXcodeIdeProject('bar2')

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':foo:foo1XcodeProject', ':foo:foo2XcodeProject', ':foo:xcode',
			':bar:bar1XcodeProject', ':bar:bar2XcodeProject', ':bar:xcode',
			':rootXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeWorkspace('root').assertHasProjects('foo/foo1.xcodeproj', 'foo/foo2.xcodeproj', 'bar/bar1.xcodeproj', 'bar/bar2.xcodeproj', 'root.xcodeproj')
	}

	def "includes Xcode projects from subprojects when only invoking the root xcode task"() {
		given:
		settingsFile << """
			rootProject.name = 'root'
			include 'foo'
			include 'bar'
		"""
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('root')
		file('foo/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('foo1') << configureXcodeIdeProject('foo2')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar1') << configureXcodeIdeProject('bar2')

		when:
		succeeds(':xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':foo:foo1XcodeProject', ':foo:foo2XcodeProject',
			':bar:bar1XcodeProject', ':bar:bar2XcodeProject',
			':rootXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeWorkspace('root').assertHasProjects('foo/foo1.xcodeproj', 'foo/foo2.xcodeproj', 'bar/bar1.xcodeproj', 'bar/bar2.xcodeproj', 'root.xcodeproj')
	}

	// TODO: Test composite build
	// TODO: Test source dependencies
	// TODO: Test duplicated project name
	// TODO: Test can include subproject Xcode project in workspace (only for project applied)
}
