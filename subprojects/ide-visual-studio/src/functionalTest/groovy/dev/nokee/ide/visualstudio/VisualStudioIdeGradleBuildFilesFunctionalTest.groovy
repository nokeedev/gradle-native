package dev.nokee.ide.visualstudio

import dev.nokee.ide.fixtures.AbstractIdeGradleBuildFilesFunctionalTest
import dev.nokee.ide.fixtures.IdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeTaskNames

class VisualStudioIdeGradleBuildFilesFunctionalTest extends AbstractIdeGradleBuildFilesFunctionalTest implements VisualStudioIdeTaskNames {
	@Override
	protected String getIdePluginId() {
		return 'dev.nokee.visual-studio-ide'
	}

	@Override
	protected String configureIdeProject(String name) {
		// Needs to be DSL-agnostic
		return """
			visualStudio {
				projects.register("${name}") {
					target(${VisualStudioIdeProjectConfiguration.canonicalName}.of(${VisualStudioIdeConfiguration.canonicalName}.of("Default"), ${VisualStudioIdePlatforms.canonicalName}.X64)) {}
				}
			}
		"""
	}

	@Override
	protected IdeProjectFixture ideProject(String name) {
		return VisualStudioIdeProjectFixture.of(file(name))
	}
}
