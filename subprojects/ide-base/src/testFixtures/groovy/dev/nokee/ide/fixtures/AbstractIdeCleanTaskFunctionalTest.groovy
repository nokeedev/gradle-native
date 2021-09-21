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
import dev.gradleplugins.test.fixtures.file.TestFile

import static dev.nokee.utils.TaskNameUtils.getShortestName

abstract class AbstractIdeCleanTaskFunctionalTest extends AbstractGradleSpecification {
	def "can clean generated IDE files"() {
		given:
		settingsFile << '''
			rootProject.name = 'root'
			include 'foo', 'bar'
		'''
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo')
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar')

		when:
		succeeds(tasks.ideLifecycle)
		then:
		ideWorkspaceFiles('root').assertExist()
		ideProjectFiles('root').assertExist()
		ideProjectFiles('foo/foo').assertExist()
		ideProjectFiles('bar/bar').assertExist()

		when:
		succeeds(tasks(':bar').ideClean)
		then:
		ideProjectFiles('bar/bar').assertDoesNotExist()
		and:
		ideWorkspaceFiles('root').assertExist()
		ideProjectFiles('root').assertExist()
		ideProjectFiles('foo/foo').assertExist()

		when:
		succeeds(tasks.ideClean.substring(1)) // Without root project path
		then:
		ideProjectFiles('bar/bar').assertDoesNotExist()
		and:
		ideWorkspaceFiles('root').assertDoesNotExist()
		ideProjectFiles('root').assertDoesNotExist()
		ideProjectFiles('foo/foo').assertDoesNotExist()
	}

	def "does not clean generated IDE files using project clean lifecycle task"() {
		given:
		settingsFile << "rootProject.name = 'foo'"
		buildFile << applyIdePlugin() << "apply plugin: 'lifecycle-base'" << configureIdeProject('foo')

		when:
		succeeds(tasks.ideLifecycle)
		then:
		ideWorkspaceFiles('foo').assertExist()
		ideProjectFiles('foo').assertExist()

		when:
		succeeds('clean')
		then:
		ideWorkspaceFiles('foo').assertExist()
		ideProjectFiles('foo').assertExist()
	}

	def "can clean generated IDE files from included builds"() {
		given:
		settingsFile << '''
			rootProject.name = 'root'
			includeBuild 'foo'
			include 'bar'
		'''
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo')
		file('foo', settingsFileName).createNewFile()
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar')

		when:
		succeeds(tasks.ideLifecycle)
		then:
		ideWorkspaceFiles('root').assertExist()
		ideProjectFiles('root').assertExist()
		ideProjectFiles('foo/foo').assertExist()
		ideProjectFiles('bar/bar').assertExist()

		when: 'can only clean root project IDE files'
		succeeds(tasks.ideClean)
		then:
		ideWorkspaceFiles('root').assertDoesNotExist()
		ideProjectFiles('root').assertDoesNotExist()
		and:
		ideProjectFiles('bar/bar').assertExist()
		ideProjectFiles('foo/foo').assertExist()

		when:
		succeeds(tasks.ideClean.substring(1)) // Without root project path
		then:
		ideWorkspaceFiles('root').assertDoesNotExist()
		ideProjectFiles('root').assertDoesNotExist()
		and:
		ideProjectFiles('bar/bar').assertDoesNotExist()
		ideProjectFiles('foo/foo').assertDoesNotExist()
	}

	def "can clean generated IDE files from included builds using task abbreviation"() {
		given:
		settingsFile << '''
			rootProject.name = 'root'
			includeBuild 'foo'
			include 'bar'
		'''
		buildFile << applyIdePlugin() << configureIdeProject('root')
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo')
		file('foo', settingsFileName).createNewFile()
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar')

		when:
		succeeds(tasks.ideLifecycle)
		then:
		ideWorkspaceFiles('root').assertExist()
		ideProjectFiles('root').assertExist()
		ideProjectFiles('foo/foo').assertExist()
		ideProjectFiles('bar/bar').assertExist()

		when:
		succeeds(getShortestName(tasks.ideClean.substring(1))) // Without root project path
		then:
		ideWorkspaceFiles('root').assertDoesNotExist()
		ideProjectFiles('root').assertDoesNotExist()
		ideProjectFiles('bar/bar').assertDoesNotExist()
		ideProjectFiles('foo/foo').assertDoesNotExist()
	}

	protected abstract String getIdePluginId()

	protected String applyIdePlugin() {
		return """
			plugins {
				id '${idePluginId}'
			}
		"""
	}

	protected abstract String configureIdeProject(String name)

	protected abstract IdeFiles ideProjectFiles(String path)

	protected abstract IdeFiles ideWorkspaceFiles(String path)

	protected static abstract class IdeFiles {
		abstract List<TestFile> get()

		void assertExist() {
			get().each {
				it.assertExists()
			}
		}

		void assertDoesNotExist() {
			get().each {
				it.assertDoesNotExist()
			}
		}
	}

	protected IdeFiles ofFiles(Object... paths) {
		return new IdeFiles() {
			@Override
			List<TestFile> get() {
				return Arrays.asList(paths).collect { file(it) }
			}
		}
	}
}
