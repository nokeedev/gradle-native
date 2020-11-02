package dev.nokee.ide.visualstudio.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(VisualStudioIdePlugin)
class VisualStudioIdeWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.visual-studio-ide'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return VisualStudioIdePlugin
	}
}
