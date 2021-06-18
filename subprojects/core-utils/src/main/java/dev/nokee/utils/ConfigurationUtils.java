package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

import java.util.Locale;
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

	/**
	 * Configures a {@link Configuration} as declarable configuration bucket.
	 * A declarable configuration bucket has both {@link Configuration#isCanBeConsumed() canBeConsumed} and {@link Configuration#isCanBeResolved() canBeResolved} to {@code false}.
	 *
	 * @return a configuration action, never null
	 */
	public static ConfigurationAction asDeclarable() {
		return ConfigurationBuckets.DECLARABLE;
	}

	/**
	 * Configures a {@link Configuration} as consumable configuration bucket, a.k.a. outgoing dependencies.
	 * A consumable bucket has {@link Configuration#isCanBeConsumed() canBeConsumed} set to {@code true} and {@link Configuration#isCanBeResolved() canBeResolved} to {@code false}.
	 *
	 * @return a configuration action, never null
	 */
	public static ConfigurationAction asConsumable() {
		return ConfigurationBuckets.CONSUMABLE;
	}

	/**
	 * Configures a {@link Configuration} as resolvable configuration bucket, a.k.a. incoming dependencies.
	 * A consumable bucket has {@link Configuration#isCanBeConsumed() canBeConsumed} set to {@code false} and {@link Configuration#isCanBeResolved() canBeResolved} to {@code true}.
	 *
	 * @return a configuration action, never null
	 */
	public static ConfigurationAction asResolvable() {
		return ConfigurationBuckets.RESOLVABLE;
	}

	public interface ConfigurationAction extends ActionUtils.Action<Configuration>, Assertable<Configuration> {}

	private enum ConfigurationBuckets implements ConfigurationAction {
		DECLARABLE(false, false),
		CONSUMABLE(true, false),
		RESOLVABLE(false, true);

		private final boolean canBeConsumed;
		private final boolean canBeResolved;

		ConfigurationBuckets(boolean canBeConsumed, boolean canBeResolved) {
			this.canBeConsumed = canBeConsumed;
			this.canBeResolved = canBeResolved;
		}

		private String getConfigurationTypeName() {
			return name().toLowerCase(Locale.CANADA);
		}

		@Override
		public void execute(Configuration configuration) {
			configuration.setCanBeConsumed(canBeConsumed);
			configuration.setCanBeResolved(canBeResolved);
		}

		@Override
		public String toString() {
			return "ConfigurationUtils.as" + StringUtils.capitalize(getConfigurationTypeName()) + "()";
		}

		@Override
		public void satisfiedBy(Context<? extends Configuration> context) {
			context.assertThat(this::bucketMatches, this::cannotReuseExistingConfiguration);
		}

		private boolean bucketMatches(Configuration configuration) {
			return configuration.isCanBeConsumed() == canBeConsumed && configuration.isCanBeResolved() == canBeResolved;
		}

		private String cannotReuseExistingConfiguration(Configuration configuration) {
			return String.format("Cannot reuse existing configuration named '%s' as a %s configuration because it does not match the expected configuration (expecting: [canBeConsumed: %s, canBeResolved: %s], actual: [canBeConsumed: %s, canBeResolved: %s]).", configuration.getName(), getConfigurationTypeName(), canBeConsumed, canBeResolved, configuration.isCanBeConsumed(), configuration.isCanBeResolved());
		}
	}
}
