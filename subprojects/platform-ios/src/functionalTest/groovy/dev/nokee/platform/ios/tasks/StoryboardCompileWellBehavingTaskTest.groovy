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
package dev.nokee.platform.ios.tasks

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.core.exec.CommandLineTool
import dev.nokee.fixtures.tasks.WellBehavingTaskAssertion
import dev.nokee.fixtures.tasks.WellBehavingTaskProperty
import dev.nokee.fixtures.tasks.WellBehavingTaskPropertyValue
import dev.nokee.fixtures.tasks.WellBehavingTaskSpec
import dev.nokee.fixtures.tasks.WellBehavingTaskTest
import dev.nokee.fixtures.tasks.WellBehavingTaskTransform
import dev.nokee.platform.ios.fixtures.elements.GenericStoryboard
import dev.nokee.platform.ios.fixtures.elements.NokeeAppBaseLanguage
import dev.nokee.platform.ios.tasks.internal.StoryboardCompileTask
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Task
import spock.lang.Requires

// TODO: Not technically right, it should be more toolchain requirements for iOS development
@Requires({ SystemUtils.IS_OS_MAC})
class StoryboardCompileWellBehavingTaskTest extends WellBehavingTaskTest {
	@Override
	protected Class<? extends Task> getTaskType() {
		return StoryboardCompileTask
	}

	protected List<WellBehavingTaskProperty> getInputTestCases() {
		def result = []
		// TODO: We should be able to tell what the property is (say input files or output directory) and it will imply some test cases... for not let's have everything declared

		// Missing coverage:
		// [ ] null/not set -> failure (validation)
		// [ ] set expected value -> success (validation)
		result << property('module')
			.configureAsProperty()
			.withInitialValue('ModuleName')
			.outOfDateWhen({it.buildFile << "${taskUnderTestDsl} { module = 'AnotherModuleName' }"})
			.build()


		// Missing coverage:
		// [ ] no files -> no source
		// [ ] transition no files to files -> rebuild -> sketch
		// [ ] non existing files -> ?
		result << property('sources')
			.configureAsFileCollection()
			.withInitialValue(WellBehavingTaskPropertyValue.GroovyDslExpression.of('project.fileTree("src/main/resources", { include("*.lproj/*.storyboard") })'))
			// Remove one file
			.incremental(describe('remove one file', {assert it.file('src/main/resources/Base.lproj/LaunchScreen.storyboard').delete()}), {
				it.file('output-directory').assertIsDirectory()
				it.file('output-directory/Base.lproj').assertIsDirectory()
				assert it.file('output-directory/Base.lproj').listFiles()*.name as Set == ['Main.storyboardc'] as Set
			})
			// Remove all files
			.incremental(describe('remove all files', {assert it.file('src/main/resources/Base.lproj').deleteDir()}), {it.file('output-directory').assertHasDescendants()})
			// Add one file
			.incremental(describe('add one file', {new GenericStoryboard('AnotherLaunchScreen').writeToProject(it.testDirectory)}), {
				it.file('output-directory').assertIsDirectory()
				it.file('output-directory/Base.lproj').assertIsDirectory()
				assert it.file('output-directory/Base.lproj').listFiles()*.name as Set == ['LaunchScreen.storyboardc', 'Main.storyboardc', 'AnotherLaunchScreen.storyboardc'] as Set
			})
			.build()

		// Not configurable, let's ignore for now
		result << property('ibtoolExecutable')
			.ignore()

		// Test cases for destination directory
		result << property('destinationDirectory')
			.configureAsProperty()
			.withInitialValue(WellBehavingTaskPropertyValue.File.of('output-directory'))
			.outOfDateWhen(deleteDirectory('output-directory'))
			.outOfDateWhen(deleteDirectory('output-directory/Base.lproj/LaunchScreen.storyboardc'))
			.outOfDateWhen(cleanDirectory('output-directory'))
			.outOfDateWhen(changeFile('output-directory/Base.lproj/LaunchScreen.storyboardc/Info.plist'))
			// Moving destination directory
			.outOfDateWhen({ WellBehavingTaskSpec spec -> spec.file('build.gradle') << 'taskUnderTest.destinationDirectory = file("another-directory")' })
			.incremental({ WellBehavingTaskSpec spec -> spec.file('build.gradle') << 'taskUnderTest.destinationDirectory = file("another-directory")' }, new WellBehavingTaskAssertion() {
				@Override
				void assertState(WellBehavingTaskSpec context) {
					context.testDirectory.file('outputDirectory').assertDoesNotExist()
					context.testDirectory.file('another-directory').assertIsDirectory() // TODO: Should really be assert hasDescendent and what not
				}
			})
			.restoreFromCacheWhen(deleteDirectory('output-directory'))
			.build()

		return result
	}

	WellBehavingTaskTransform describe(String description, WellBehavingTaskTransform transform) {
		return new WellBehavingTaskTransform() {
			@Override
			void applyChanges(WellBehavingTaskSpec context) {
				transform.applyChanges(context)
			}

			@Override
			String getDescription() {
				return description
			}
		}
	}

	@Override
	void assertInitialState() {
		file('output-directory').assertIsDirectory()
		assert file('output-directory').listFiles().size() == 1
		def baseDirectory = TestFile.of(file('output-directory').listFiles()[0])
		baseDirectory.assertIsDirectory()
		assert baseDirectory.listFiles()*.name as Set == ['LaunchScreen.storyboardc', 'Main.storyboardc'] as Set
	}

	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		new NokeeAppBaseLanguage().writeToProject(testDirectory)

		// TODO: Maybe we should just move the initial value here and remove all the Mutator and Value modeling...
		buildFile << """
			import ${CommandLineTool.canonicalName}
			${taskUnderTestDsl} {
				interfaceBuilderTool = CommandLineTool.of('/usr/bin/ibtool')
			}
		"""
	}
}
