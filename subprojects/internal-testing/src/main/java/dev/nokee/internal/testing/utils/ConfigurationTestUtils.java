package dev.nokee.internal.testing.utils;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

import java.util.function.Consumer;

import static dev.nokee.internal.testing.utils.TestUtils.rootProject;

public final class ConfigurationTestUtils {
	private ConfigurationTestUtils() {}

	public static Configuration testConfiguration() {
		return rootProject().getConfigurations().create("test");
	}

	public static Configuration testConfiguration(Consumer<? super Configuration> action) {
		val configuration = testConfiguration();
		action.accept(configuration);
		return configuration;
	}

	public static Configuration testConfiguration(Action<? super Configuration> action) {
		val configuration = testConfiguration();
		action.execute(configuration);
		return configuration;
	}
}
