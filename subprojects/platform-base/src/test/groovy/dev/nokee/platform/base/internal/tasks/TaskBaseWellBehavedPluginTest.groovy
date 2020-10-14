package dev.nokee.platform.base.internal.tasks

import dev.gradleplugins.fixtures.WellBehavedPluginTest
import dev.nokee.platform.base.internal.plugins.TaskBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.junit.Assume

class TaskBaseWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	protected String getQualifiedPluginId() {
		Assume.assumeTrue(false) // no qualified plugin id
		throw new UnsupportedOperationException()
	}

	@Override
	protected Class<? extends Plugin<Project>> getPluginType() {
		return TaskBasePlugin
	}
}
