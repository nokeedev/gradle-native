package dev.nokee.language.cpp.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(CppLanguageBasePlugin)
class CppLanguageBaseWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.cpp-language-base'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return CppLanguageBasePlugin
	}
}
