/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.ide.fixtures

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import spock.lang.Unroll

abstract class AbstractIdeGradleBuildFilesFunctionalTest extends AbstractGradleSpecification {
	@Unroll
	def "includes #buildFileName and #settingsFileName in IDE projects source"(buildFileName, settingsFileName, expectedSourceLayout) {
		given:
		file(settingsFileName).createNewFile()
		file(buildFileName) << applyIdePlugin() << configureIdeProject('foo')

		when:
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToIde('foo'))
		ideProject('foo').assertHasBuildFiles(*expectedSourceLayout)

		where:
		buildFileName 		| settingsFileName 		| expectedSourceLayout
		'build.gradle' 		| 'settings.gradle' 	| ['build.gradle', 'settings.gradle']
		'build.gradle'		| 'settings.gradle.kts'	| ['build.gradle', 'settings.gradle.kts']
		'build.gradle.kts'	| 'settings.gradle'		| ['build.gradle.kts', 'settings.gradle']
		'build.gradle.kts'	| 'settings.gradle.kts'	| ['build.gradle.kts', 'settings.gradle.kts']
	}

	def "includes build file modified through the command line in IDE project source"() {
		given:
		def buildFile = file('foo.gradle')
		buildFile << applyIdePlugin() << configureIdeProject('foo')

		when:
		executer = executer.usingBuildScript(buildFile).withoutDeprecationChecks()
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToIde('foo'))
		ideProject('foo').assertHasBuildFiles('foo.gradle', 'settings.gradle')
	}

	def "includes settings file modified through the command line in IDE project source"() {
		given:
		def settingsFile = file('foo-settings.gradle').createFile()
		buildFile << applyIdePlugin() << configureIdeProject('foo')

		when:
		executer = executer.usingSettingsFile(settingsFile).withoutDeprecationChecks()
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToIde('foo'))
		ideProject('foo').assertHasBuildFiles('build.gradle', 'foo-settings.gradle')
	}

	def "includes gradle.properties file when available in IDE project source"() {
		given:
		buildFile << applyIdePlugin() << configureIdeProject('foo')
		file('gradle.properties').createFile()

		when:
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToIde('foo'))
		ideProject('foo').assertHasBuildFiles('build.gradle', 'settings.gradle', 'gradle.properties')
	}

	def "includes all project build files when available in all IDE project source"() {
		given:
		buildFile << applyIdePlugin() << configureIdeProject('foo') << configureIdeProject('bar')
		file('gradle.properties').createFile()

		when:
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToIde('bar', 'foo'))
		ideProject('foo').assertHasBuildFiles('build.gradle', 'settings.gradle', 'gradle.properties')
		ideProject('bar').assertHasBuildFiles('build.gradle', 'settings.gradle', 'gradle.properties')
	}

	def "does not include gradle.properties file when available in IDE project source of subprojects"() {
		given:
		settingsFile << 'include "bar"'
		buildFile << applyIdePlugin() << configureIdeProject('foo')
		file('bar/build.gradle') << applyIdePlugin() << configureIdeProject('bar')
		file('gradle.properties').createNewFile()

		when:
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks(':bar').allToIdeProject('bar'), tasks.allToIde('foo'))
		ideProject('bar/bar').assertHasBuildFiles('build.gradle')
	}

	@Unroll
	def "does not include settings.gradle file in IDE project source of subprojects"(shouldUseKotlinDsl) {
		given:
		if (shouldUseKotlinDsl) {
			useKotlinDsl()
		}
		settingsFile << 'include("bar")'
		buildFile << applyIdePlugin() << configureIdeProject('foo')
		file('bar/build.gradle') << applyIdePlugin() << configureIdeProject('bar')

		when:
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks(':bar').allToIdeProject('bar'), tasks.allToIde('foo'))
		ideProject('bar/bar').assertHasBuildFiles('build.gradle')

		where:
		shouldUseKotlinDsl << [true, false]
	}

	def "does not include missing build.gradle file in IDE project source"() {
		given:
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << """
			gradle.rootProject {
				apply plugin: '${idePluginId}'

				${configureIdeProject('foo')}
			}
		"""

		when:
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToIde('foo'))
		ideProject('foo').assertHasBuildFiles('settings.gradle')
	}

	def "includes init scripts passed on the command line in IDE project source of the root project only"() {
		given:
		settingsFile << 'include("bar")'
		buildFile << applyIdePlugin() << configureIdeProject('foo')
		file('bar/build.gradle') << applyIdePlugin() << configureIdeProject('bar')

		and:
		def initFooFile = file('init-foo.gradle').createFile()
		def initBarFile = file('init-bar.gradle').createFile()

		when:
		executer = executer.usingInitScript(initFooFile).usingInitScript(initBarFile)
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks(':bar').allToIdeProject('bar'), tasks.allToIde('foo'))
		ideProject('foo').assertHasBuildFiles('build.gradle', 'settings.gradle', 'init-foo.gradle', 'init-bar.gradle')
		ideProject('bar/bar').assertHasBuildFiles('build.gradle')
	}

	protected abstract String getIdePluginId()

	protected String applyIdePlugin() {
		// Needs to be DSL-agnostic
		return """
			plugins {
				id("${idePluginId}")
			}
		"""
	}

	// Needs to be DSL-agnostic
	protected abstract String configureIdeProject(String name)

	protected abstract IdeProjectFixture ideProject(String name)
}
