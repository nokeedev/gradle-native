package dev.nokee.ide.xcode

import spock.lang.Unroll

class XcodeIdeGradleBuildFilesFunctionalTest extends AbstractXcodeIdeFunctionalSpec {
	@Unroll
	def "includes #buildFileName and #settingsFileName in Xcode projects source"(buildFileName, settingsFileName, expectedSourceLayout) {
		given:
		file(settingsFileName).createNewFile()
		file(buildFileName) << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('foo').assertHasSourceLayout('Products/Foo.app', *expectedSourceLayout)

		where:
		buildFileName 		| settingsFileName 		| expectedSourceLayout
		'build.gradle' 		| 'settings.gradle' 	| ['build.gradle', 'settings.gradle']
		'build.gradle'		| 'settings.gradle.kts'	| ['build.gradle', 'settings.gradle.kts']
		'build.gradle.kts'	| 'settings.gradle'		| ['build.gradle.kts', 'settings.gradle']
		'build.gradle.kts'	| 'settings.gradle.kts'	| ['build.gradle.kts', 'settings.gradle.kts']
	}

	def "includes build file modified through the command line in Xcode project source"() {
		given:
		def buildFile = file('foo.gradle')
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')

		when:
		executer = executer.usingBuildScript(buildFile)
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('foo').assertHasSourceLayout('Products/Foo.app', 'foo.gradle', 'settings.gradle')
	}

	def "includes settings file modified through the command line in Xcode project source"() {
		given:
		def settingsFile = file('foo-settings.gradle').createFile()
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')

		when:
		executer = executer.usingSettingsFile(settingsFile)
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('foo').assertHasSourceLayout('Products/Foo.app', 'build.gradle', 'foo-settings.gradle')
	}

	def "includes gradle.properties file when available in Xcode project source"() {
		given:
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')
		file('gradle.properties').createFile()

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('foo').assertHasSourceLayout('Products/Foo.app', 'build.gradle', 'settings.gradle', 'gradle.properties')
	}

	def "includes all project build files when available in all Xcode project source"() {
		given:
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo') << configureXcodeIdeProject('bar')
		file('gradle.properties').createFile()

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':barXcodeProject', ':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('foo').assertHasSourceLayout('Products/Foo.app', 'build.gradle', 'settings.gradle', 'gradle.properties')
		xcodeProject('bar').assertHasSourceLayout('Products/Bar.app', 'build.gradle', 'settings.gradle', 'gradle.properties')
	}

	def "does not include gradle.properties file when available in Xcode project source of subprojects"() {
		given:
		settingsFile << 'include "bar"'
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar')
		file('gradle.properties').createNewFile()

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':bar:barXcodeProject', ':bar:xcode',
			':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('bar/bar').assertHasSourceLayout('Products/Bar.app', 'build.gradle')
	}

	@Unroll
	def "does not include settings.gradle file in Xcode project source of subprojects"(shouldUseKotlinDsl) {
		given:
		if (shouldUseKotlinDsl) {
			useKotlinDsl()
		}
		settingsFile << 'include("bar")'
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar')

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':bar:barXcodeProject', ':bar:xcode', ':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('bar/bar').assertHasSourceLayout('Products/Bar.app', 'build.gradle')

		where:
		shouldUseKotlinDsl << [true, false]
	}

	def "does not include missing build.gradle file in Xcode project source"() {
		given:
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << """
			gradle.rootProject {
				apply plugin: 'dev.nokee.xcode-ide'

				${configureXcodeIdeProject('foo')}
			}
		"""

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('foo').assertHasSourceLayout('Products/Foo.app', 'settings.gradle')
	}

	def "includes init scripts passed on the command line in Xcode project source of the root project only"() {
		given:
		settingsFile << 'include("bar")'
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar')

		and:
		def initFooFile = file('init-foo.gradle').createFile()
		def initBarFile = file('init-bar.gradle').createFile()

		when:
		executer = executer.usingInitScript(initFooFile).usingInitScript(initBarFile)
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':bar:barXcodeProject', ':bar:xcode', ':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('foo').assertHasSourceLayout('Products/Foo.app', 'build.gradle', 'settings.gradle', 'init-foo.gradle', 'init-bar.gradle')
		xcodeProject('bar/bar').assertHasSourceLayout('Products/Bar.app', 'build.gradle')
	}
}
