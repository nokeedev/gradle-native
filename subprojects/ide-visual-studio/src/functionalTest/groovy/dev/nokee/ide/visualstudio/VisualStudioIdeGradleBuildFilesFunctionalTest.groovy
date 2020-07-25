package dev.nokee.ide.visualstudio

import dev.nokee.ide.fixtures.AbstractIdeGradleBuildFilesFunctionalTest
import dev.nokee.ide.fixtures.IdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeTaskNames

class VisualStudioIdeGradleBuildFilesFunctionalTest extends AbstractIdeGradleBuildFilesFunctionalTest implements VisualStudioIdeTaskNames, VisualStudioIdeFixture {
	@Override
	protected String getIdePluginId() {
		return visualStudioIdePluginId
	}

	@Override
	protected String configureIdeProject(String name) {
		return configureVisualStudioIdeProject(name)
	}

	@Override
	protected IdeProjectFixture ideProject(String name) {
		return VisualStudioIdeProjectFixture.of(file(name))
	}
}
