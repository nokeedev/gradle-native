package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.HasConfigurableAttributes;
import org.gradle.api.attributes.Usage;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class ConfigurationUtils {
	private ConfigurationUtils() {}

	public static final Attribute<String> ARTIFACT_TYPE_ATTRIBUTE = Attribute.of("artifactType", String.class);

	public static ActionUtils.Action<Configuration> configureDescription(Supplier<? extends String> description) {
		return new ConfigureDescriptionAction(description);
	}

	public static ActionUtils.Action<Configuration> configureDescription(String format, Object... args) {
		requireNonNull(format);
		requireNonNull(args);
		return new ConfigureDescriptionAction(() -> String.format(format, args));
	}

	@EqualsAndHashCode
	private static final class ConfigureDescriptionAction implements ActionUtils.Action<Configuration> {
		private final Supplier<? extends String> description;

		public ConfigureDescriptionAction(Supplier<? extends String> description) {
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

	/**
	 * Configures a {@link Configuration}'s attributes.
	 *
	 * @param action  an action to configure attributes details, must not be null
	 * @param <T>  the attributes configurable type
	 * @return a configuration action, never null
	 */
	public static <T extends HasConfigurableAttributes<T>> ActionUtils.Action<T> configureAttributes(Consumer<? super AttributesDetails> action) {
		return new ConfigureAttributesAction<>(action);
	}

	/**
	 * Configures a {@link Configuration}'s attributes from the specified {@link AttributesProvider} object.
	 * It's a short hand version of {@literal configureAttributes(it -> it.from(obj))}.
	 *
	 * @param obj  an attributes provider object, must not be null
	 * @param <T>  the attributes configurable type
	 * @return a configuration action, never null
	 * @see #configureAttributes(Consumer)
	 */
	public static <T extends HasConfigurableAttributes<T>> ActionUtils.Action<T> attributesOf(Object obj) {
		requireNonNull(obj);
		return new ConfigureAttributesAction<>(it -> it.from(obj));
	}

	public interface AttributesDetails {
		AttributesDetails usage(Usage usage);
		AttributesDetails from(Object obj);
		AttributesDetails artifactType(String artifactType);
	}

	/** @see #configureAttributes(Consumer) */
	@EqualsAndHashCode
	private static final class ConfigureAttributesAction<T extends HasConfigurableAttributes<T>> implements ActionUtils.Action<T> {
		private final Consumer<? super AttributesDetails> action;

		public ConfigureAttributesAction(Consumer<? super AttributesDetails> action) {
			this.action = requireNonNull(action);
		}

		@Override
		public void execute(T configuration) {
			action.accept(new DefaultConfigurationAttributeBuilder(configuration));
		}

		@Override
		public String toString() {
			return "ConfigurationUtils.configureAttributes(" + action + ")";
		}

		private static final class DefaultConfigurationAttributeBuilder implements AttributesDetails {
			private final HasConfigurableAttributes<?> configuration;

			public DefaultConfigurationAttributeBuilder(HasConfigurableAttributes<?> configuration) {
				this.configuration = configuration;
			}

			public AttributesDetails usage(Usage usage) {
				configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, usage);
				return this;
			}

			public AttributesDetails from(Object obj) {
				if (configuration instanceof Configuration && obj instanceof AttributesProvider) {
					if (ConfigurationBuckets.RESOLVABLE.isSatisfiedBy((Configuration) configuration)) {
						((AttributesProvider) obj).forResolving(configuration.getAttributes());
					} else if (ConfigurationBuckets.CONSUMABLE.isSatisfiedBy((Configuration) configuration)) {
						((AttributesProvider) obj).forConsuming(configuration.getAttributes());
					} else {
						throw new IllegalStateException(String.format("Configuration '%s' must be either consumable or resolvable.", ((Configuration) configuration).getName()));
					}
				}
				return this;
			}

			public AttributesDetails artifactType(String artifactType) {
				configuration.getAttributes().attribute(ARTIFACT_TYPE_ATTRIBUTE, artifactType);
				return this;
			}
		}
	}

	/**
	 * Provides attributes on {@link HasConfigurableAttributes} object.
	 * @see #configureAttributes(Consumer)
	 */
	public interface AttributesProvider {
		void forConsuming(AttributeContainer attributes);
		void forResolving(AttributeContainer attributes);
	}
}
