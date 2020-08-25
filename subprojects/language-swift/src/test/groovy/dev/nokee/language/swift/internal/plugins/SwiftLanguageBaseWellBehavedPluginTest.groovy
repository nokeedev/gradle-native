package dev.nokee.language.swift.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(SwiftLanguageBasePlugin)
class SwiftLanguageBaseWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.swift-language-base'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return SwiftLanguageBasePlugin
	}
}
