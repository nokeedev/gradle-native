package dev.nokee.language.objectivecpp.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(ObjectiveCppLanguagePlugin)
class ObjectiveCppLanguageWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.objective-cpp-language'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return ObjectiveCppLanguagePlugin
	}
}
