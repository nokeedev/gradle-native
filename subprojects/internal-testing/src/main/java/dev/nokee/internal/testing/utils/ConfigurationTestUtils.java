package dev.nokee.internal.testing.utils;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

import static dev.nokee.internal.testing.utils.TestUtils.rootProject;

public final class ConfigurationTestUtils {
	private ConfigurationTestUtils() {}

	public static Configuration testConfiguration() {
		return rootProject().getConfigurations().create("test");
	}

	public static Configuration testConfiguration(String name) {
		return rootProject().getConfigurations().create(name);
	}

	public static Configuration testConfiguration(Action<? super Configuration> action) {
		val configuration = testConfiguration();
		action.execute(configuration);
		return configuration;
	}
}
