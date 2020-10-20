package dev.nokee.language.c.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(CLanguagePlugin)
class CLanguageWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.c-language'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return CLanguagePlugin
	}
}
