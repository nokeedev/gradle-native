package dev.nokee.language.objectivec.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import spock.lang.Subject

@Subject(ObjectiveCLanguageBasePlugin)
class ObjectiveCLanguageBaseWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		return 'dev.nokee.objective-c-language-base'
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return ObjectiveCLanguageBasePlugin
	}
}
