package dev.nokee.ide.visualstudio

import dev.gradleplugins.runnerkit.GradleRunner
import dev.nokee.ide.fixtures.IdeCommandLineUtils
import dev.nokee.ide.visualstudio.fixtures.MSBuildExecutor
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeSolutionFixture

import java.util.function.UnaryOperator

trait VisualStudioIdeFixture {
	String getVisualStudioIdePluginId() {
		return 'dev.nokee.visual-studio-ide'
	}

	// Must be kotlin dsl compatible
	String applyVisualStudioIdePlugin() {
		return """
			plugins {
				id("${visualStudioIdePluginId}")
			}
		"""
	}

	// Must be kotlin dsl compatible
	String configureVisualStudioIdeProject(String name) {
		return """
			visualStudio {
				projects.register("${name}") {
					target(${VisualStudioIdeProjectConfiguration.canonicalName}.of(${VisualStudioIdeConfiguration.canonicalName}.of("Default"), ${VisualStudioIdePlatforms.canonicalName}.X64)) {}
				}
			}
		"""
	}

	VisualStudioIdeSolutionFixture visualStudioSolution(String path) {
		return VisualStudioIdeSolutionFixture.of(file(path))
	}

	VisualStudioIdeProjectFixture visualStudioProject(String path) {
		return VisualStudioIdeProjectFixture.of(file(path))
	}

	MSBuildExecutor getMsbuild() {
		// Gradle needs to be isolated so the msbuild does not leave behind daemons
		assert executer.usesGradleDistribution()
		assert !executer.usesSharedDaemons()
		assert executer.usesDaemon()
		return new MSBuildExecutor(testDirectory)
	}

	UnaryOperator<GradleRunner> getMsbuildTool() {
		return { executer ->
			def initScript = file("init.gradle")
			initScript << IdeCommandLineUtils.generateGradleProbeInitFile('visualStudio', 'msbuild')
			return executer
				.requireIsolatedDaemons()
				.requireGradleDistribution()
				.requireDaemon()
				.usingInitScript(initScript)
		}
	}
}
