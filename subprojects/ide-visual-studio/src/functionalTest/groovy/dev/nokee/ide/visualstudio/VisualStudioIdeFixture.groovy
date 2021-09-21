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
//		assert executer.usesGradleDistribution()
//		assert !executer.usesSharedDaemons()
//		assert executer.usesDaemon()
		return new MSBuildExecutor(testDirectory)
	}

	UnaryOperator<GradleRunner> getMsbuildTool() {
		return { executer ->
			def initScript = file("init.gradle")
			initScript << IdeCommandLineUtils.generateGradleProbeInitFile('visualStudio', 'msbuild')
			return executer
//				.requireIsolatedDaemons()
//				.requireGradleDistribution()
//				.requireDaemon()
				.usingInitScript(initScript)
		}
	}
}
