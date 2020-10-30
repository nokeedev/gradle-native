package dev.nokee.core.exec

import dev.gradleplugins.fixtures.gradle.GradleFixture
import dev.gradleplugins.fixtures.gradle.runner.GradleExecutor
import dev.gradleplugins.fixtures.gradle.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CommandLineToolTaskInputFunctionalTest extends Specification implements GradleFixture {
	@Rule
	TemporaryFolder temporaryFolder = new TemporaryFolder()

	def setup() {
		buildFile << """
			buildscript {
				dependencies {
					classpath ${configurePluginClasspathAsFileCollection()}
				}
			}
		"""
	}

	@Override
	GradleRunner newRunner() {
		return GradleRunner.create(GradleExecutor.gradleTestKit())
	}

	@Override
	File getTestDirectory() {
		return temporaryFolder.root
	}

	def "can use command line tool from path as task input"() {
		given:
		buildFile << declareTaskWithToolInput() << """
			compile.tool = CommandLineTools.fromPath('gcc')
		"""

		expect:
		succeeds('compile').executedTaskPaths == [':compile']
		succeeds('compile').skippedTaskPaths == [':compile']

		and:
		buildFile << """
			compile.tool = CommandLineTools.fromPath('g++')
		"""
		succeeds('compile').executedTaskPaths == [':compile']
	}

	def "can use command line tool from location as task input"() {
		given:
		file('gcc') << 'some executable'
		buildFile << declareTaskWithToolInput() << """
			compile.tool = CommandLineTools.fromLocation(file('gcc'))
		"""

		expect:
		succeeds('compile').executedTaskPaths == [':compile']
		succeeds('compile').skippedTaskPaths == [':compile']

		and:
		file('gcc') << 'changes'
		succeeds('compile').executedTaskPaths == [':compile']
	}

	protected String declareTaskWithToolInput() {
		return """
			import ${CommandLineTool.canonicalName}
			import ${CommandLineTools.canonicalName}

			abstract class MyCompileTask extends DefaultTask {
				@Nested
				abstract Property<CommandLineTool> getTool();

				@OutputFile
				abstract RegularFileProperty getOutputFile();

				@TaskAction
				private void doAction() {
					outputFile.asFile.get() << tool.get().executable
				}
			}

			tasks.register('compile', MyCompileTask) {
				outputFile = layout.buildDirectory.file('out')
			}
		"""
	}
}
