package dev.nokee.internal.testing.testers;

import dev.nokee.internal.testing.utils.TestUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public abstract class WellBehavedPluginApplyByTypeTester {
	protected abstract Class<? extends Plugin<?>> getPluginTypeUnderTest();

	@Test
	void canApplyPluginByTypeUsingProjectApply() {
		Project project = TestUtils.rootProject();
		project.apply(Collections.singletonMap("plugin", getPluginTypeUnderTest()));
	}
}
