package dev.nokee.language.c.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project

class CLanguageBaseWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.c-language-base'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return CLanguageBasePlugin
	}
}
