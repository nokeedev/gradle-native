package dev.nokee.language.objectivec.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.junit.Assume
import spock.lang.Subject

@Subject(ObjectiveCLanguageBasePlugin)
class ObjectiveCLanguageBaseWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		Assume.assumeTrue(false) // no qualified plugin id
		throw new UnsupportedOperationException()
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return ObjectiveCLanguageBasePlugin
	}
}
