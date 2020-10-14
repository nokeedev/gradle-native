package dev.nokee.platform.base.internal.binaries

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import dev.nokee.platform.base.internal.plugins.BinaryBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.junit.Assume

class BinaryBaseWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		Assume.assumeTrue(false) // no qualified plugin id
		throw new UnsupportedOperationException()
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return BinaryBasePlugin
	}
}
