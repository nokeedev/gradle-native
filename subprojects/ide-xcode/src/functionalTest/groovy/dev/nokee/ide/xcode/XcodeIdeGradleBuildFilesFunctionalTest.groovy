package dev.nokee.ide.xcode

import dev.nokee.ide.fixtures.AbstractIdeGradleBuildFilesFunctionalTest
import dev.nokee.ide.fixtures.IdeProjectFixture
import dev.nokee.ide.xcode.fixtures.XcodeIdeProjectFixture
import dev.nokee.ide.xcode.fixtures.XcodeIdeTaskNames

class XcodeIdeGradleBuildFilesFunctionalTest extends AbstractIdeGradleBuildFilesFunctionalTest implements XcodeIdeTaskNames, XcodeIdeFixture {
	@Override
	protected String getIdePluginId() {
		return xcodeIdePluginId
	}

	@Override
	protected String configureIdeProject(String name) {
		return configureXcodeIdeProject(name)
	}

	@Override
	protected IdeProjectFixture ideProject(String name) {
		return XcodeIdeProjectFixture.of(file(name))
	}
}
