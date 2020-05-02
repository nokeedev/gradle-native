package dev.nokee.ide.xcode.internal.plugins

import dev.gradleplugins.integtests.fixtures.WellBehavedPluginTest

class XcodeIdePluginWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	String getQualifiedPluginId() {
		return 'dev.nokee.xcode-ide'
	}

	@Override
	String getMainTask() {
		return 'xcode'
	}
}
