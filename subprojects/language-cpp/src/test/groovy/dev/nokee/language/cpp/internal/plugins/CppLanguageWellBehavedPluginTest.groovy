package dev.nokee.language.cpp.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(CppLanguagePlugin)
class CppLanguageWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.cpp-language'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return CppLanguagePlugin
	}
}
