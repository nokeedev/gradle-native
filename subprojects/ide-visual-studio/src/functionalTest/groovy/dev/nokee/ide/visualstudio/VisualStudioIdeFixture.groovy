package dev.nokee.ide.visualstudio

import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeSolutionFixture

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
}
