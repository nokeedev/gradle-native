package dev.nokee.ide.xcode

import dev.nokee.ide.fixtures.AbstractIdeLifecycleTasksFunctionalTest
import dev.nokee.ide.xcode.fixtures.XcodeIdeTaskNames
import dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture

class XcodeIdeLifecycleTasksFunctionalTest extends AbstractIdeLifecycleTasksFunctionalTest implements XcodeIdeFixture, XcodeIdeTaskNames {
	@Override
	protected String getIdeWorkspaceDisplayNameUnderTest() {
		return 'Xcode workspace'
	}

	@Override
	protected String workspaceName(String name) {
		return XcodeIdeWorkspaceFixture.workspaceName(name)
	}

	@Override
	protected String getIdeUnderTestDsl() {
		return 'xcode'
	}

	@Override
	protected String configureIdeProject(String name) {
		return configureXcodeIdeProject(name)
	}

	@Override
	protected String getIdePluginId() {
		return xcodeIdePluginId
	}
}
