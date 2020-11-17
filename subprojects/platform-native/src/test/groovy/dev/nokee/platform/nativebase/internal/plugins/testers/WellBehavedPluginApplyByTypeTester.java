package dev.nokee.platform.nativebase.internal.plugins.testers;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public abstract class WellBehavedPluginApplyByTypeTester {
	protected abstract Class<? extends Plugin<?>> getPluginTypeUnderTest();

	@Test
	void canApplyPluginByTypeUsingProjectApply() {
		Project project = ProjectBuilder.builder().build();
		project.apply(Collections.singletonMap("plugin", getPluginTypeUnderTest()));
	}
}
