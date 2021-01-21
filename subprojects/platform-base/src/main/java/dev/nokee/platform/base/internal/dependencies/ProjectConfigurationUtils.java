package dev.nokee.platform.base.internal.dependencies;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class ProjectConfigurationUtils {
	private static final ThreadLocal<ObjectFactory> OBJECT_FACTORY_THREAD_LOCAL = new ThreadLocal<>();

	private static ObjectFactory getObjectFactory() {
		return requireNonNull(OBJECT_FACTORY_THREAD_LOCAL.get());
	}

	private ProjectConfigurationUtils() {}

	/**
	 * Configures a {@link Configuration} for specified {@link Usage} attribute.
	 *
	 * @param usage  the usage attribute value, must not be null
	 * @return a configuration action, never null
	 */
	public static Consumer<Configuration> forUsage(String usage) {
		return attribute(Usage.USAGE_ATTRIBUTE, named(usage));
	}

	private static final Attribute<String> ARTIFACT_FORMAT = Attribute.of("artifactType", String.class);

	/**
	 * Configures a {@link Configuration} for specified artifact format attribute.
	 *
	 * @param artifactFormat  the artifact format attribute value, must not be null
	 * @return a configuration action, never null
	 */
	public static Consumer<Configuration> forArtifactFormat(String artifactFormat) {
		return attribute(ARTIFACT_FORMAT, ofInstance(artifactFormat));
	}

	/**
	 * Configures a {@link Configuration} for specified {@link DocsType} attribute.
	 *
	 * @param docsType  the documentation type attribute value, must not be null
	 * @return a configuration action, never null
	 */
	public static Consumer<Configuration> forDocsType(String docsType) {
		return attribute(DocsType.DOCS_TYPE_ATTRIBUTE, named(docsType));
	}

	/**
	 * Delegates the configuration for a {@link Configuration} using the specified {@link ObjectFactory}.
	 * It allows users with a {@link ProjectConfigurationRegistry} instance to configure the attributes without explicitly having access to an {@link ObjectFactory} using {@link #forUsage(String)} and {@link #attribute(Attribute, AttributeFactory)} or to access a {@link ObjectFactory} instance via {@link #withObjectFactory(BiConsumer)}.
	 *
	 * @param objectFactory  the object factory to use in the thread context, must not be null
	 * @param action  a delegate configuration action, must not be null
	 * @return a configuration action, never null
	 */
	public static Action<Configuration> using(ObjectFactory objectFactory, Consumer<? super Configuration> action) {
		requireNonNull(objectFactory);
		requireNonNull(action);
		return configuration -> {
			val previousValue = OBJECT_FACTORY_THREAD_LOCAL.get();
			OBJECT_FACTORY_THREAD_LOCAL.set(objectFactory);
			try {
				action.accept(configuration);
			} finally {
				OBJECT_FACTORY_THREAD_LOCAL.set(previousValue);
			}
		};
	}

	/**
	 * Configures a {@link Configuration} with access to a {@link ObjectFactory}.
	 *
	 * @param action  a configuration action, must not be null
	 * @return a configuration action, never null
	 */
	public static Consumer<Configuration> withObjectFactory(BiConsumer<? super Configuration, ObjectFactory> action) {
		return new WithObjectFactoryConfigurationConsumer(action);
	}

	@EqualsAndHashCode
	private static final class WithObjectFactoryConfigurationConsumer implements ConfigurationConsumer {
		private final BiConsumer<? super Configuration, ObjectFactory> action;

		private WithObjectFactoryConfigurationConsumer(BiConsumer<? super Configuration, ObjectFactory> action) {
			this.action = requireNonNull(action);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void assertValue(Configuration value) {
			if (action instanceof Assertable) {
				((Assertable<Configuration>) action).assertValue(value);
			}
		}

		@Override
		public void accept(Configuration configuration) {
			action.accept(configuration, getObjectFactory());
		}

		@Override
		public String toString() {
			return "ProjectConfigurationUtils.withObjectFactory(" + action + ")";
		}
	}

	/**
	 * Attribute factory for {@link Configuration}.
	 *
	 * @param <T> the attribute type
	 */
	public interface AttributeFactory<T> {
		T create(ObjectFactory objectFactory, Class<T> attributeType);
	}

	/**
	 * Configures the attribute of a {@link Configuration}.
	 *
	 * @param attribute  the attribute to configure, must not be null
	 * @param factory  the factory to create the attribute value, must not be null
	 * @param <T>  the attribute type
	 * @return a configuration action, never null
	 */
	public static <T> Consumer<Configuration> attribute(Attribute<T> attribute, AttributeFactory<T> factory) {
		return new AttributeConfigurationConsumer<>(attribute, factory);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class AttributeConfigurationConsumer<T> extends ConfigurationWithObjectFactoryConsumer {
		private final Attribute<T> attribute;
		private final AttributeFactory<T> factory;

		private AttributeConfigurationConsumer(Attribute<T> attribute, AttributeFactory<T> factory) {
			this.attribute = requireNonNull(attribute);
			this.factory = requireNonNull(factory);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void assertValue(Configuration configuration) {
			val value = configuration.getAttributes().getAttribute(attribute);
			if (value == null) {
				throw new IllegalStateException(String.format("Cannot reuse existing configuration named '%s' because it does not have attribute '%s' of type '%s'.", configuration.getName(), attribute.getName(), attribute.getType().getCanonicalName()));
			} else if (factory instanceof Assertable) {
				try {
					((Assertable<T>) factory).assertValue(value);
				} catch (Throwable ex) {
					throw new IllegalStateException(String.format("Cannot reuse existing configuration named '%s' because of an unexpected value for attribute '%s' of type '%s'.", configuration.getName(), attribute.getName(), attribute.getType().getCanonicalName()), ex);
				}
			}
		}

		@Override
		protected void accept(Configuration configuration, ObjectFactory objectFactory) {
			configuration.getAttributes().attribute(attribute, factory.create(objectFactory, attribute.getType()));
		}

		@Override
		public String toString() {
			return "ProjectConfigurationUtils.attribute(" + attribute +	", " + factory + ")";
		}
	}

	/**
	 * Returns a factory to create a {@link Named} instance of the specified name.
	 *
	 * @param name  the name of the {@link Named} instance, must not be null
	 * @param <T>  the attribute type
	 * @return an attribute factory, never null
	 */
	public static <T extends Named> AttributeFactory<T> named(String name) {
		return new NamedAttributeFactory<>(name);
	}

	@EqualsAndHashCode
	private static final class NamedAttributeFactory<T extends Named> implements AttributeFactory<T>, Assertable<T> {
		private final String attributeName;

		private NamedAttributeFactory(String attributeName) {
			this.attributeName = requireNonNull(attributeName);
		}

		@Override
		public T create(ObjectFactory objectFactory, Class<T> attributeType) {
			return objectFactory.named(attributeType, attributeName);
		}

		@Override
		public void assertValue(T value) {
			if (!value.getName().equals(attributeName)) {
				throw new IllegalStateException(String.format("Unexpected attribute value (expecting: %s, actual: %s).", attributeName, value.getName()));
			}
		}

		@Override
		public String toString() {
			return "named(" + attributeName + ")";
		}
	}


	/**
	 * Returns a factory of the specified instance.
	 *
	 * @param instance  the instance to return from the factory, must not be null
	 * @param <T>  the attribute type
	 * @return an attribute factory, never null
	 */
	public static <T> AttributeFactory<T> ofInstance(T instance) {
		return new OfInstanceAttributeFactory<>(instance);
	}

	@EqualsAndHashCode
	private static final class OfInstanceAttributeFactory<T> implements AttributeFactory<T>, Assertable<T> {
		private final T instance;

		private OfInstanceAttributeFactory(T instance) {
			this.instance = requireNonNull(instance);
		}

		@Override
		public T create(ObjectFactory objectFactory, Class<T> attributeType) {
			assert attributeType.isInstance(instance);
			return instance;
		}

		@Override
		public void assertValue(T value) {
			if (!instance.equals(value)) {
				throw new IllegalStateException(String.format("Unexpected attribute value (expecting: %s, actual: %s).", instance, value));
			}
		}

		@Override
		public String toString() {
			return "ofInstance(" + instance + ")";
		}
	}

	private interface Assertable<T> {
		void assertValue(T value);
	}

	/**
	 * Assert the specified configuration is configured according to the specified action.
	 *
	 * @param configuration  a configuration to assert, must not be null
	 * @param action  a action that doubles as a configuration specification
	 * @return the configuration to assert, never null
	 */
	@SuppressWarnings("unchecked")
	public static Configuration assertConfigured(Configuration configuration, Consumer<? super Configuration> action) {
		if (action instanceof Assertable) {
			((Assertable<Configuration>) action).assertValue(configuration);
		}
		return configuration;
	}

//	public static Consumer<Configuration> artifact(Object artifact) {
//		return new ArtifactConfigurationConsumer(artifact);
//	}
//
//	private static final class ArtifactConfigurationConsumer implements ConfigurationConsumer {
//		private final Object artifact;
//
//		private ArtifactConfigurationConsumer(Object artifact) {
//			this.artifact = requireNonNull(artifact);
//		}
//
//		@Override
//		public void assertValue(Configuration configuration) {
//			throw new UnsupportedOperationException("artifact assertion of existing configuration is not supported");
//		}
//
//		@Override
//		public void accept(Configuration configuration) {
//			configuration.getOutgoing().artifact(artifact);
//		}
//	}

	/**
	 * Configures the description of a {@link Configuration}.
	 *
	 * @param description  the description, must not be null
	 * @return a configuration action, never null
	 */
	public static Consumer<Configuration> description(String description) {
		return new DescriptionConfigurationConsumer(Suppliers.ofInstance(requireNonNull(description)));
	}

	/**
	 * Configures the description of a {@link Configuration}.
	 *
	 * @param descriptionSupplier  the description supplier, must not be null
	 * @return a configuration action, never null
	 */
	public static Consumer<Configuration> description(Supplier<? extends String> descriptionSupplier) {
		return new DescriptionConfigurationConsumer(descriptionSupplier);
	}

	@EqualsAndHashCode
	private static final class DescriptionConfigurationConsumer implements ConfigurationConsumer {
		private final Supplier<? extends String> descriptionSupplier;

		private DescriptionConfigurationConsumer(Supplier<? extends String> descriptionSupplier) {
			this.descriptionSupplier = requireNonNull(descriptionSupplier);
		}

		@Override
		public void assertValue(Configuration value) {
			// do nothing, because a different description is not a meaningful difference
		}

		@Override
		public void accept(Configuration configuration) {
			configuration.setDescription(descriptionSupplier.get());
		}

		@Override
		public String toString() {
			return "ProjectConfigurationUtils.description(" + descriptionSupplier + ")";
		}
	}

	/**
	 * Configures a {@link Configuration} with the specified parent configuration.
	 *
	 * @param configurations  the parent configuration to configure, must not be null
	 * @return  a configuration action, never null
	 */
	public static Consumer<Configuration> extendsFrom(Configuration... configurations) {
		return new ExtendsFromConsumer(ImmutableList.copyOf(configurations));
	}

	@EqualsAndHashCode
	private static final class ExtendsFromConsumer implements ConfigurationConsumer {
		private final List<Configuration> configurations;

		public ExtendsFromConsumer(List<Configuration> configurations) {
			this.configurations = configurations;
		}

		@Override
		public void assertValue(Configuration value) {
			if (!value.getExtendsFrom().containsAll(configurations)) {
				val missingConfiguration = new ArrayList<>(configurations);
				missingConfiguration.removeAll(value.getExtendsFrom());
				throw new IllegalStateException(String.format("Missing parent configuration: %s", missingConfiguration.stream().map(Configuration::getName).collect(Collectors.joining(", "))));
			}
		}

		@Override
		public void accept(Configuration configuration) {
			configuration.extendsFrom(configurations.toArray(new Configuration[0]));
		}

		@Override
		public String toString() {
			return "ProjectConfigurationUtils.extendsFrom(" + configurations + ")";
		}
	}

	/**
	 * Configures a {@link Configuration} as declarable configuration bucket.
	 *
	 * @return a configuration action, never null
	 */
	public static Consumer<Configuration> asDeclarable() {
		return ConfigurationBuckets.DECLARABLE;
	}

	/**
	 * Configures a {@link Configuration} as consumable configuration bucket.
	 *
	 * @return a configuration action, never null
	 */
	public static Consumer<Configuration> asConsumable() {
		return ConfigurationBuckets.CONSUMABLE;
	}

	/**
	 * Configures a {@link Configuration} as resolvable configuration bucket.
	 *
	 * @return a configuration action, never null
	 */
	public static Consumer<Configuration> asResolvable() {
		return ConfigurationBuckets.RESOLVABLE;
	}

	private enum ConfigurationBuckets implements ConfigurationConsumer {
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

		public void assertValue(Configuration configuration) {
			if (configuration.isCanBeConsumed() != canBeConsumed || configuration.isCanBeResolved() != canBeResolved) {
				throw new IllegalStateException(String.format("Cannot reuse existing configuration named '%s' as a %s configuration because it does not match the expected configuration (expecting: [canBeConsumed: %s, canBeResolved: %s], actual: [canBeConsumed: %s, canBeResolved: %s]).", configuration.getName(), getConfigurationTypeName(), canBeConsumed, canBeResolved, configuration.isCanBeConsumed(), configuration.isCanBeResolved()));
			}
		}

		@Override
		public void accept(Configuration configuration) {
			configuration.setCanBeConsumed(canBeConsumed);
			configuration.setCanBeResolved(canBeResolved);
		}

		@Override
		public String toString() {
			return "ProjectConfigurationUtils.as" + StringUtils.capitalize(getConfigurationTypeName()) + "()";
		}
	}

	private static abstract class ConfigurationWithObjectFactoryConsumer implements ConfigurationConsumer {
		@Override
		public final void accept(Configuration configuration) {
			accept(configuration, getObjectFactory());
		}

		protected abstract void accept(Configuration configuration, ObjectFactory objectFactory);
	}

	private interface ConfigurationConsumer extends Consumer<Configuration>, Assertable<Configuration> {
		default Consumer<Configuration> andThen(Consumer<? super Configuration> after) {
			return new AndThenConfigurationConsumer(this, requireNonNull(after));
		}
	}

	private static final class AndThenConfigurationConsumer implements ConfigurationConsumer {
		private final Consumer<? super Configuration> first;
		private final Consumer<? super Configuration> second;

		private AndThenConfigurationConsumer(Consumer<? super Configuration> first, Consumer<? super Configuration> second) {
			this.first = first;
			this.second = second;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void assertValue(Configuration configuration) {
			if (first instanceof Assertable) {
				((Assertable<Configuration>) first).assertValue(configuration);
			}
			if (second instanceof Assertable) {
				((Assertable<Configuration>) second).assertValue(configuration);
			}
		}

		@Override
		public void accept(Configuration configuration) {
			first.accept(configuration);
			second.accept(configuration);
		}
	}
}
