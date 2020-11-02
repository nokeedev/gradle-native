package dev.nokee.ide.xcode.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(XcodeIdeBasePlugin)
class XcodeIdeBaseWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.xcode-ide-base'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return XcodeIdeBasePlugin
	}
}
