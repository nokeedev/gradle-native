package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class ConfigurationUtils {
	private ConfigurationUtils() {}

	public static Action<Configuration> configureDescription(Supplier<String> description) {
		return new ConfigureDescriptionAction(description);
	}

	@EqualsAndHashCode
	private static final class ConfigureDescriptionAction implements Action<Configuration> {
		private final Supplier<String> description;

		public ConfigureDescriptionAction(Supplier<String> description) {
			this.description = requireNonNull(description);
		}

		@Override
		public void execute(Configuration configuration) {
			configuration.setDescription(description.get());
		}

		@Override
		public String toString() {
			return "ConfigurationUtils.configureDescription(" + description + ")";
		}
	}
}
