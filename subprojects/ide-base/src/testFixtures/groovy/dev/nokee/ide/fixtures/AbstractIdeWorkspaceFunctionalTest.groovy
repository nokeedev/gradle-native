/*
 * Copyright 2020 the original author or authors.
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

abstract class AbstractIdeWorkspaceFunctionalTest extends AbstractGradleSpecification {
	def "can generate empty IDE workspace"() {
		given:
		settingsFile << configureRootProjectName()
		buildFile << applyIdePlugin()

		expect:
		def result = succeeds(tasks.ideWorkspace)
		result.assertTasksExecutedAndNotSkipped(tasks.ideWorkspace)
		ideWorkspaceUnderTest.assertHasProjects()
	}

	def "uses root project name as IDE workspace filename"() {
		given: 'a single-project build'
		settingsFile << configureRootProjectName()
		buildFile << applyIdePlugin()

		when:
		def result = succeeds(tasks.ideWorkspace)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.ideWorkspace)
		testDirectory.listFiles(exceptHiddenFilesAndMavenLocalRepository())*.name as Set == ['build.gradle', 'settings.gradle', workspaceName(rootProjectName)] as Set
	}

	def "includes IDE workspace from current project"() {
		given: 'a single-project build'
		settingsFile << configureRootProjectName()
		buildFile << applyIdePlugin() << configureIdeProject('root')

		when:
		def result = succeeds(tasks.ideWorkspace)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.ideWorkspace, tasks.ideProject('root'))

		and:
		ideWorkspaceUnderTest.assertHasProjects('root')
	}

	def "includes IDE projects from Gradle subprojects"() {
		given: 'a multi-project build'
		settingsFile << configureRootProjectName() << """
			include 'foo'
			include 'bar'
		"""
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo1') << configureIdeProject('foo2')
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar1') << configureIdeProject('bar2')

		when:
		succeeds(tasks.ideWorkspace)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.ideWorkspace, tasks.ideProject('root'), tasks(':foo').allToIdeProject('foo1', 'foo2'), tasks(':bar').allToIdeProject('bar1', 'bar2'))
		ideWorkspaceUnderTest.assertHasProjects('foo1', 'foo2', 'bar1', 'bar2', 'root')
	}

	def "build IDE workspace when invoking the root IDE lifecycle task"() {
		given: 'a multi-project build'
		settingsFile << configureRootProjectName() << """
			include 'foo'
			include 'bar'
		"""
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo1') << configureIdeProject('foo2')
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar1') << configureIdeProject('bar2')

		when:
		succeeds(tasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToIde('root'), tasks(':foo').allToIdeProject('foo1', 'foo2'), tasks(':bar').allToIdeProject('bar1', 'bar2'))
		ideWorkspaceUnderTest.assertHasProjects('foo1', 'foo2', 'bar1', 'bar2', 'root')
	}

	def "includes IDE projects from Gradle included builds"() {
		given: 'a multi-project composite build'
		settingsFile << configureRootProjectName() << """
			includeBuild 'foo'
			includeBuild 'bar'
		"""
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('foo', settingsFileName).createFile()
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo1') << configureIdeProject('foo2')
		file('bar', settingsFileName).createFile()
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar1') << configureIdeProject('bar2')

		when:
		succeeds(tasks.ideWorkspace)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.ideWorkspace, tasks.ideProject('root'), tasks(':foo').allToIdeProject('foo1', 'foo2'), tasks(':bar').allToIdeProject('bar1', 'bar2'))
		ideWorkspaceUnderTest.assertHasProjects('foo1', 'foo2', 'bar1', 'bar2', 'root')
	}

	def "can have Gradle project without IDE plugin applied"() {
		given:
		settingsFile << configureRootProjectName() << """
			include 'foo'
			include 'bar'
		"""
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo1') << configureIdeProject('foo2')
		file('bar', buildFileName).createFile()

		when:
		succeeds(tasks.ideWorkspace)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.ideWorkspace, tasks.ideProject('root'), tasks(':foo').allToIdeProject('foo1', 'foo2'))
		ideWorkspaceUnderTest.assertHasProjects('foo1', 'foo2', 'root')
	}

	def "can have Gradle project without IDE project configured"() {
		given:
		settingsFile << configureRootProjectName() << """
			include 'foo'
			include 'bar'
		"""
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo1') << configureIdeProject('foo2')
		file('bar', buildFileName) << applyIdePlugin()

		when:
		succeeds(tasks.ideWorkspace)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.ideWorkspace, tasks.ideProject('root'), tasks(':foo').allToIdeProject('foo1', 'foo2'))
		ideWorkspaceUnderTest.assertHasProjects('foo1', 'foo2', 'root')
	}

	def "can remap IDE projects to include in IDE workspace"() {
		given: 'a multi-project build'
		settingsFile << configureRootProjectName() << """
			include 'foo'
			include 'bar'
		"""
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo1') << configureIdeProject('foo2')
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar1') << configureIdeProject('bar2')

		and:
		buildFile << """
			${ideUnderTestDsl} {
				workspace.projects = provider { allprojects*.${ideUnderTestDsl}*.projects.flatten().findAll { it.name.endsWith('1') } }
			}
		"""

		when:
		succeeds(tasks.ideWorkspace)

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.ideWorkspace, tasks(':foo').allToIdeProject('foo1'), tasks(':bar').allToIdeProject('bar1'))
		ideWorkspaceUnderTest.assertHasProjects('foo1', 'bar1')
	}

	// TODO: Test duplicated name
	// TODO: Remove stale workspace file when project change name

	protected abstract String configureIdeProject(String name)

	protected String getRootProjectName() {
		return 'root'
	}

	protected String configureRootProjectName() {
		return """
			rootProject.name = '${rootProjectName}'
		"""
	}

	private static FilenameFilter exceptHiddenFilesAndMavenLocalRepository() {
		return new FilenameFilter() {
			@Override
			boolean accept(File dir, String name) {
				return !name.startsWith('.') && !name.equals('m2-home-should-not-be-filled')
			}
		}
	}

	protected abstract String getIdeUnderTestDsl()

	protected abstract String workspaceName(String name)

	protected abstract IdeWorkspaceFixture getIdeWorkspaceUnderTest()

	protected abstract String getIdePluginId()

	protected String applyIdePlugin() {
		return """
			plugins {
				id '${idePluginId}'
			}
		"""
	}
}
