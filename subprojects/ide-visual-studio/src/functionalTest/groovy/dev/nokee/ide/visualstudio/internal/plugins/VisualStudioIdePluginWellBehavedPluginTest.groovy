package dev.nokee.ide.visualstudio.internal.plugins

import dev.gradleplugins.integtests.fixtures.WellBehavedPluginTest
import spock.lang.Subject

@Subject(VisualStudioIdePlugin)
class VisualStudioIdePluginWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	String getQualifiedPluginId() {
		return 'dev.nokee.visual-studio-ide'
	}

	@Override
	String getMainTask() {
		return 'visualStudio'
	}
}
