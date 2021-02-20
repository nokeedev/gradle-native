package dev.nokee.internal.testing.utils;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

import static dev.nokee.internal.testing.utils.TestUtils.rootProject;

public final class ConfigurationTestUtils {
	private static final String TEST_CONFIGURATION_NAME = "test";
	private ConfigurationTestUtils() {}

	public static Configuration testConfiguration() {
		return testConfiguration(TEST_CONFIGURATION_NAME);
	}

	public static Configuration testConfiguration(String name) {
		return rootProject().getConfigurations().create(name);
	}

	public static Configuration testConfiguration(Action<? super Configuration> action) {
		return testConfiguration(TEST_CONFIGURATION_NAME, action);
	}

	public static Configuration testConfiguration(String name, Action<? super Configuration> action) {
		val configuration = testConfiguration(name);
		action.execute(configuration);
		return configuration;
	}
}
