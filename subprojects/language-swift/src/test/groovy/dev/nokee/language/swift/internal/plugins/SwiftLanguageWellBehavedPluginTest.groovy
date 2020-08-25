package dev.nokee.language.swift.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(SwiftLanguagePlugin)
class SwiftLanguageWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.swift-language'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return SwiftLanguagePlugin
	}
}
