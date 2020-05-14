package dev.nokee.platform.ios.tasks

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.core.exec.CommandLineTool
import dev.nokee.platform.ios.fixtures.elements.GenericStoryboard
import dev.nokee.platform.ios.tasks.fixtures.WellBehavingTaskAssertion
import dev.nokee.platform.ios.tasks.fixtures.WellBehavingTaskProperty
import dev.nokee.platform.ios.tasks.fixtures.WellBehavingTaskPropertyValue
import dev.nokee.platform.ios.tasks.fixtures.WellBehavingTaskSpec
import dev.nokee.platform.ios.tasks.internal.StoryboardCompileTask
import dev.nokee.platform.ios.fixtures.BaseLanguage
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
			.incremental({assert it.file('src/main/resources/Base.lproj/LaunchScreen.storyboard').delete()}, {
				it.file('output-directory').assertIsDirectory()
				it.file('output-directory/Base.lproj').assertIsDirectory()
				assert it.file('output-directory/Base.lproj').listFiles()*.name as Set == ['Main.storyboardc'] as Set
			})
			// Remove all files
			.incremental({assert it.file('src/main/resources/Base.lproj').deleteDir()}, {it.file('output-directory').assertHasDescendants()})
			// Add one file
			.incremental({new GenericStoryboard('AnotherLaunchScreen').writeToProject(it.testDirectory)}, {
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
		new BaseLanguage().writeToProject(testDirectory)

		// TODO: Maybe we should just move the initial value here and remove all the Mutator and Value modeling...
		buildFile << """
			import ${CommandLineTool.canonicalName}
			${taskUnderTestDsl} {
				interfaceBuilderTool = CommandLineTool.of('/usr/bin/ibtool')
			}
		"""
	}
}
