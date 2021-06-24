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
	private static final class ConfigureDescriptionAction implements ActionUtils.Action<Configuration> {
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
	public static ActionUtils.Action<Configuration> asDeclarable() {
		return ConfigurationActions.DECLARABLE;
	}

	/**
	 * Matches a declarable {@link Configuration} bucket.
	 *
	 * @return a configuration spec, never null
	 */
	public static SpecUtils.Spec<Configuration> declarable() {
		return ConfigurationSpecs.DECLARABLE;
	}

	/**
	 * Configures a {@link Configuration} as consumable configuration bucket, a.k.a. outgoing dependencies.
	 * A consumable bucket has {@link Configuration#isCanBeConsumed() canBeConsumed} set to {@code true} and {@link Configuration#isCanBeResolved() canBeResolved} to {@code false}.
	 *
	 * @return a configuration action, never null
	 */
	public static ActionUtils.Action<Configuration> asConsumable() {
		return ConfigurationActions.CONSUMABLE;
	}

	/**
	 * Matches a declarable {@link Configuration} bucket.
	 *
	 * @return a configuration spec, never null
	 */
	public static SpecUtils.Spec<Configuration> consumable() {
		return ConfigurationSpecs.CONSUMABLE;
	}

	/**
	 * Configures a {@link Configuration} as resolvable configuration bucket, a.k.a. incoming dependencies.
	 * A consumable bucket has {@link Configuration#isCanBeConsumed() canBeConsumed} set to {@code false} and {@link Configuration#isCanBeResolved() canBeResolved} to {@code true}.
	 *
	 * @return a configuration action, never null
	 */
	public static ActionUtils.Action<Configuration> asResolvable() {
		return ConfigurationActions.RESOLVABLE;
	}

	/**
	 * Matches a declarable {@link Configuration} bucket.
	 *
	 * @return a configuration spec, never null
	 */
	public static SpecUtils.Spec<Configuration> resolvable() {
		return ConfigurationSpecs.RESOLVABLE;
	}

	/**
	 * @see #declarable()
	 * @see #consumable()
	 * @see #resolvable()
	 */
	private enum ConfigurationSpecs implements SpecUtils.Spec<Configuration> {
		DECLARABLE(ConfigurationBuckets.DECLARABLE),
		CONSUMABLE(ConfigurationBuckets.CONSUMABLE),
		RESOLVABLE(ConfigurationBuckets.RESOLVABLE);

		private final ConfigurationBuckets bucket;

		ConfigurationSpecs(ConfigurationBuckets bucket) {
			this.bucket = bucket;
		}

		@Override
		public boolean isSatisfiedBy(Configuration configuration) {
			return bucket.isSatisfiedBy(configuration);
		}

		@Override
		public String toString() {
			return "ConfigurationUtils." + bucket.getConfigurationTypeName() + "()";
		}
	}

	/**
	 * @see #asDeclarable()
	 * @see #asConsumable()
	 * @see #asResolvable()
	 */
	private enum ConfigurationActions implements ActionUtils.Action<Configuration> {
		DECLARABLE(ConfigurationBuckets.DECLARABLE),
		CONSUMABLE(ConfigurationBuckets.CONSUMABLE),
		RESOLVABLE(ConfigurationBuckets.RESOLVABLE);

		private final ConfigurationBuckets bucket;

		ConfigurationActions(ConfigurationBuckets bucket) {
			this.bucket = bucket;
		}

		@Override
		public void execute(Configuration configuration) {
			bucket.execute(configuration);
		}

		@Override
		public String toString() {
			return "ConfigurationUtils.as" + StringUtils.capitalize(bucket.getConfigurationTypeName()) + "()";
		}
	}

	private enum ConfigurationBuckets {
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

		public void execute(Configuration configuration) {
			configuration.setCanBeConsumed(canBeConsumed);
			configuration.setCanBeResolved(canBeResolved);
		}

		public boolean isSatisfiedBy(Configuration configuration) {
			return configuration.isCanBeConsumed() == canBeConsumed && configuration.isCanBeResolved() == canBeResolved;
		}
	}
}
