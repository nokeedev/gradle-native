package dev.nokee.language.base.internal.plugins

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.junit.Assume

class LanguageBaseWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		Assume.assumeTrue(false) // no qualified plugin id
		throw new UnsupportedOperationException()
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return LanguageBasePlugin
	}
}
