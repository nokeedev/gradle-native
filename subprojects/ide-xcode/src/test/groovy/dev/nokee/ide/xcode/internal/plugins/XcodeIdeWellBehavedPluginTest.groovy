package dev.nokee.ide.xcode.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(XcodeIdePlugin)
class XcodeIdeWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.xcode-ide'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return XcodeIdePlugin
	}
}
