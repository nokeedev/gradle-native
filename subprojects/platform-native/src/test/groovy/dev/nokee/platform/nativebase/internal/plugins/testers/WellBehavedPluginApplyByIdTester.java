package dev.nokee.platform.nativebase.internal.plugins.testers;

import dev.nokee.internal.testing.utils.TestUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

public abstract class WellBehavedPluginApplyByIdTester {
	protected abstract String getQualifiedPluginIdUnderTest();
	protected abstract Class<? extends Plugin<?>> getPluginTypeUnderTest();

	private final Project project = TestUtils.rootProject();

	private void applyPluginUnderTest() {
		project.apply(singletonMap("plugin", getQualifiedPluginIdUnderTest()));
	}

	@Test
	void canApplyPluginByIdUsingProjectApply() {
		applyPluginUnderTest();
	}

	@Test
	void appliedPluginIdIsExpectedType() {
		applyPluginUnderTest();
		Plugin<?> pluginById = project.getPlugins().findPlugin(getQualifiedPluginIdUnderTest());
		assertThat(pluginById, isA(getPluginTypeUnderTest()));
	}
}
