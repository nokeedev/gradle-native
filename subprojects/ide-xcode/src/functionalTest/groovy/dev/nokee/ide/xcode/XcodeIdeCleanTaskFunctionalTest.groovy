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
package dev.nokee.ide.xcode

import dev.nokee.ide.fixtures.AbstractIdeCleanTaskFunctionalTest
import dev.nokee.ide.xcode.fixtures.XcodeIdeTaskNames

import static dev.nokee.ide.xcode.fixtures.XcodeIdeProjectFixture.projectName
import static dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture.workspaceName

class XcodeIdeCleanTaskFunctionalTest extends AbstractIdeCleanTaskFunctionalTest implements XcodeIdeTaskNames, XcodeIdeFixture {
	@Override
	protected String getIdePluginId() {
		return xcodeIdePluginId
	}

	@Override
	protected String configureIdeProject(String name) {
		return configureXcodeIdeProject(name)
	}

	@Override
	protected IdeFiles ideProjectFiles(String path) {
		return ofFiles(projectName(path))
	}

	@Override
	protected IdeFiles ideWorkspaceFiles(String path) {
		return ofFiles(workspaceName(path))
	}

	def "cleans Xcode derived data when using IDE clean task"() {
		given:
		settingsFile << '''
			rootProject.name = 'root'
			include 'foo', 'bar'
		'''
		buildFile << applyIdePlugin() << configureIdeProject('root') << '''
			apply plugin: 'lifecycle-base'
		'''
		file('foo', buildFileName) << applyIdePlugin() << configureIdeProject('foo')
		file('bar', buildFileName) << applyIdePlugin() << configureIdeProject('bar')

		and:
		run('xcode')

		and: 'create derived data directory'
		file('.gradle/XcodeDerivedData').createDirectory()

		when: 'clean keeps Xcode derived data'
		succeeds('clean')
		then:
		file('.gradle/XcodeDerivedData').assertIsDirectory()

		when: 'clean Xcode deletes derived data'
		succeeds('cleanXcode')
		then:
		file('.gradle/XcodeDerivedData').assertDoesNotExist()
		file('.gradle').assertExists()
	}
}
