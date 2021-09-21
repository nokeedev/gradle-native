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
package dev.nokee.ide.visualstudio

import dev.nokee.ide.fixtures.AbstractIdeCleanTaskFunctionalTest
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeTaskNames

import static dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture.filtersName
import static dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture.projectName
import static dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeSolutionFixture.solutionName

class VisualStudioIdeCleanTaskFunctionalTest extends AbstractIdeCleanTaskFunctionalTest implements VisualStudioIdeTaskNames, VisualStudioIdeFixture {
	@Override
	protected String getIdePluginId() {
		return visualStudioIdePluginId
	}

	@Override
	protected String configureIdeProject(String name) {
		return configureVisualStudioIdeProject(name)
	}

	@Override
	protected IdeFiles ideProjectFiles(String path) {
		return ofFiles(projectName(path), filtersName(path))
	}

	@Override
	protected IdeFiles ideWorkspaceFiles(String path) {
		return ofFiles(solutionName(path))
	}

	def "cleans Visual Studio IDE user files when present"() {
		given:
		buildFile << applyIdePlugin() << configureIdeProject('foo')

		and:
		run(tasks.ideLifecycle)

		and:
		def userProjectFile = file('foo.vcxproj.user').createFile()

		when:
		succeeds(tasks.ideClean)

		then:
		userProjectFile.assertDoesNotExist()
	}

	def "cleans .vs directory when present"() {
		given:
		settingsFile << "rootProject.name = 'foo'"
		buildFile << applyIdePlugin() << configureIdeProject('foo')

		and:
		run(tasks.ideLifecycle)

		and:
		def dotvsDirectory = visualStudioSolution('foo').dotvsDirectory

		when:
		succeeds(tasks.ideClean)

		then:
		dotvsDirectory.assertDoesNotExist()
	}

	def "warns what to do when any files inside .vs directory is locked"() {
		given:
		settingsFile << "rootProject.name = 'foo'"
		buildFile << applyIdePlugin() << configureIdeProject('foo')

		and:
		run(tasks.ideLifecycle)

		and:
		def dotvsDirectory = visualStudioSolution('foo').dotvsDirectory
		def lock = dotvsDirectory.simulateVisualStudioIdeLock()

		when:
		def failure = fails(tasks.ideClean)

		then:
		failure.assertHasDescription("Execution failed for task ':cleanVisualStudio'.")
		failure.assertHasCause("Please close your Visual Studio IDE before executing 'cleanVisualStudio'.")

		and:
		dotvsDirectory.assertExists()

		cleanup:
		lock?.close()
	}
}
