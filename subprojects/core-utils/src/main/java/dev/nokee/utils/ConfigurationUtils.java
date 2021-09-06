package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.HasConfigurableAttributes;
import org.gradle.api.attributes.Usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class ConfigurationUtils {
	private ConfigurationUtils() {}

	public static final Attribute<String> ARTIFACT_TYPE_ATTRIBUTE = Attribute.of("artifactType", String.class);

	/**
	 * Returns an action that configures the {@link Configuration} description from supplier.
	 *
	 * @param descriptionSupplier  a description supplier, must not be null
	 * @return an action that configures the {@link Configuration}'s description, never null
	 */
	public static ActionUtils.Action<Configuration> configureDescription(Supplier<? extends String> descriptionSupplier) {
		return new ConfigureDescriptionAction(descriptionSupplier);
	}

	/**
	 * Returns an action that configures the {@link Configuration} description as formatted string.
	 *
	 * @param format  string format, must not be null
	 * @param args  string format arguments, must not be null
	 * @return an action that configures the {@link Configuration}'s description, never null
	 */
	public static ActionUtils.Action<Configuration> configureDescription(String format, Object... args) {
		return new ConfigureDescriptionAction(new StringFormatSupplier(format, args));
	}

	@EqualsAndHashCode
	private static final class ConfigureDescriptionAction implements ActionUtils.Action<Configuration> {
		private final Supplier<? extends String> descriptionSupplier;

		public ConfigureDescriptionAction(Supplier<? extends String> descriptionSupplier) {
			this.descriptionSupplier = requireNonNull(descriptionSupplier);
		}

		@Override
		public void execute(Configuration configuration) {
			configuration.setDescription(descriptionSupplier.get());
		}

		@Override
		public String toString() {
			return "ConfigurationUtils.configureDescription(" + descriptionSupplier.get() + ")";
		}
	}

	/**
	 * Configures a {@link Configuration} as declarable configuration bucket.
	 * A declarable configuration bucket has both {@link Configuration#isCanBeConsumed() canBeConsumed} and {@link Configuration#isCanBeResolved() canBeResolved} to {@code false}.
	 *
	 * @return a configuration action, never null
	 */
	public static ActionUtils.Action<Configuration> configureAsDeclarable() {
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
	public static ActionUtils.Action<Configuration> configureAsConsumable() {
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
	public static ActionUtils.Action<Configuration> configureAsResolvable() {
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
	 * @see #configureAsDeclarable()
	 * @see #configureAsConsumable()
	 * @see #configureAsResolvable()
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
			return "ConfigurationUtils.configureAs" + StringUtils.capitalize(bucket.getConfigurationTypeName()) + "()";
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
	public static <T extends HasConfigurableAttributes<T>> ActionUtils.Action<T> configureAttributes(Consumer<? super AttributesDetails<T>> action) {
		return new ConfigureAttributesAction<>(action);
	}

	/**
	 * Configures a {@link Configuration}'s attributes from the specified {@link ConfigurationAttributesProvider} object.
	 *
	 * @param obj  an attributes provider object, must not be null
	 * @return a configuration action, never null
	 * @see #configureAttributes(Consumer)
	 */
	public static ActionUtils.Action<Configuration> configureAttributesFrom(Object obj) {
		requireNonNull(obj);
		if (obj instanceof ConfigurationAttributesProvider) {
			return new ConfigureAttributesFromAction((ConfigurationAttributesProvider) obj);
		}
		return ActionUtils.doNothing();
	}

	@EqualsAndHashCode
	private static final class ConfigureAttributesFromAction implements ActionUtils.Action<Configuration> {
		private final ConfigurationAttributesProvider provider;

		private ConfigureAttributesFromAction(ConfigurationAttributesProvider provider) {
			this.provider = provider;
		}

		@Override
		public void execute(Configuration configuration) {
			if (ConfigurationBuckets.RESOLVABLE.isSatisfiedBy(configuration)) {
				configuration.attributes(provider::forResolving);
			} else if (ConfigurationBuckets.CONSUMABLE.isSatisfiedBy(configuration)) {
				configuration.attributes(provider::forConsuming);
			} else {
				throw new IllegalStateException(String.format("Configuration '%s' must be either consumable or resolvable.", configuration.getName()));
			}
		}

		@Override
		public String toString() {
			return "ConfigurationUtils.configureAttributesFrom(" + provider + ")";
		}
	}

	public interface AttributesDetails<T> {
		AttributesDetails<T> usage(Usage usage);
		AttributesDetails<T> artifactType(String artifactType);
		<S> AttributesDetails<T> attribute(Attribute<S> key, S value);
	}

	public interface AttributesDetailsInternal<T> extends AttributesDetails<T> {
		T get();
	}

	/** @see #configureAttributes(Consumer) */
	@EqualsAndHashCode
	private static final class ConfigureAttributesAction<T extends HasConfigurableAttributes<T>> implements ActionUtils.Action<T> {
		private final Consumer<? super AttributesDetails<T>> action;

		public ConfigureAttributesAction(Consumer<? super AttributesDetails<T>> action) {
			this.action = requireNonNull(action);
		}

		@Override
		public void execute(T t) {
			t.attributes(new DefaultConfigurationAttributeBuilder<>(action));
		}

		@Override
		public String toString() {
			return "ConfigurationUtils.configureAttributes(" + action + ")";
		}

		private static final class DefaultConfigurationAttributeBuilder<T> implements AttributesDetails<T>, Action<AttributeContainer> {
			private final Consumer<? super AttributesDetails<T>> action;
			private AttributeContainer attributes;

			public DefaultConfigurationAttributeBuilder(Consumer<? super AttributesDetails<T>> action) {
				this.action = action;
			}

			@Override
			public void execute(AttributeContainer attributes) {
				assert this.attributes == null;
				this.attributes = attributes;
				try {
					action.accept(this);
				} finally {
					this.attributes = null;
				}
			}

			public AttributesDetails<T> usage(Usage usage) {
				attributes.attribute(Usage.USAGE_ATTRIBUTE, usage);
				return this;
			}

			public AttributesDetails<T> artifactType(String artifactType) {
				attributes.attribute(ARTIFACT_TYPE_ATTRIBUTE, artifactType);
				return this;
			}

			@Override
			public <S> AttributesDetails<T> attribute(Attribute<S> key, S value) {
				attributes.attribute(key, value);
				return this;
			}
		}
	}

	/**
	 * Provides attributes on {@link HasConfigurableAttributes} object.
	 * @see #configureAttributes(Consumer)
	 */
	public interface ConfigurationAttributesProvider {
		void forConsuming(AttributeContainer attributes);
		void forResolving(AttributeContainer attributes);
	}

	/**
	 * Configures a {@link Configuration} with the specified parent configuration.
	 *
	 * @param configurations  the parent configuration to configure, must not be null
	 * @return  a configuration action, never null
	 */
	public static ActionUtils.Action<Configuration> configureExtendsFrom(Object... configurations) {
		return new ConfigureExtendsFromAction(ImmutableList.copyOf(configurations));
	}

	@EqualsAndHashCode
	private static final class ConfigureExtendsFromAction implements ActionUtils.Action<Configuration> {
		private final List<Object> configurations;

		public ConfigureExtendsFromAction(List<Object> configurations) {
			this.configurations = configurations;
		}

		private List<Configuration> getConfigurations() {
			return DeferredUtils.flatUnpackUntil(configurations, Configuration.class);
		}

		@Override
		public void execute(Configuration configuration) {
			val configurations = new ArrayList<>(getConfigurations());
			configurations.remove(configuration);
			configuration.extendsFrom(configurations.toArray(new Configuration[0]));
		}

		@Override
		public String toString() {
			return "ConfigurationUtils.configureExtendsFrom(" + configurations.stream().map(Objects::toString).collect(Collectors.joining(", ")) + ")";
		}
	}
}
