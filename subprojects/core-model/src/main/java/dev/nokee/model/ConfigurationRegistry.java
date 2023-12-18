/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model;

import dev.nokee.utils.ActionUtils;
import dev.nokee.utils.ConfigurationUtils;
import dev.nokee.utils.NamedDomainObjectCollectionUtils;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ConfigurationRegistry implements NamedDomainObjectRegistry<Configuration>, PolymorphicDomainObjectRegistry<Configuration> {
	private final Map<String, NamedDomainObjectRegistry<Configuration>> REGISTRY_CACHE = new HashMap<>();
	private static final DefaultRegistrableType CONFIGURATION_REGISTRABLE_TYPES = new DefaultRegistrableType();
	private final ConfigurationContainer container;

	public ConfigurationRegistry(ConfigurationContainer container) {
		this.container = container;
	}

	@Override
	public NamedDomainObjectProvider<Configuration> register(String name) {
		return container.register(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends Configuration> NamedDomainObjectProvider<S> register(String name, Class<S> type) throws InvalidUserDataException {
		return (NamedDomainObjectProvider<S>) ConfigurationType.forType(type).asRegistry(container).register(name);
	}

	@Override
	public NamedDomainObjectProvider<Configuration> registerIfAbsent(String name) {
		return NamedDomainObjectCollectionUtils.registerIfAbsent(container, name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S extends Configuration> NamedDomainObjectProvider<S> registerIfAbsent(String name, Class<S> type) {
		return (NamedDomainObjectProvider<S>) ConfigurationType.forType(type).asRegistry(container).registerIfAbsent(name);
	}

	public NamedDomainObjectProvider<Configuration> register(String name, ConfigurationType type) {
		return type.asRegistry(container).register(name);
	}

	public NamedDomainObjectProvider<Configuration> registerIfAbsent(String name, ConfigurationType type) {
		return type.asRegistry(container).registerIfAbsent(name);
	}

	public enum ConfigurationType {
		Configuration(new LegacyConfigurationFactory()),
		ConsumableConfiguration("consumable", ConfigurationUtils.configureAsConsumable()),
		DependencyScopeConfiguration("dependencyScope", ConfigurationUtils.configureAsDeclarable()),
		ResolvableConfiguration("resolvable", ConfigurationUtils.configureAsResolvable())
		;

		private ConfigurationTypeRegistryFactory factory;

		ConfigurationType() {
			this(new LegacyConfigurationFactory());
		}

		ConfigurationType(String factoryMethodName, Action<? super Configuration> legacyConfigureAction) {
			this(Optional.ofNullable(tryFindCreateMethod(factoryMethodName)).map(it -> (ConfigurationTypeRegistryFactory) new FactoryMethodBasedRegistry("ConsumableConfiguration", it)).orElse(new LegacyConfigurationFactory(legacyConfigureAction)));
		}

		ConfigurationType(ConfigurationTypeRegistryFactory factory) {
			this.factory = factory;
		}

		private NamedDomainObjectRegistry<Configuration> asRegistry(ConfigurationContainer container) {
			return factory.newRegistry(container);
		}

		private static ConfigurationType forType(Class<? extends Configuration> type) {
			return Arrays.stream(ConfigurationType.values())
				.filter(it -> it.name().equals(type.getSimpleName()))
				.findFirst()
				.orElseThrow(() -> new UnsupportedOperationException("registration type must be Configuration or ConsumableConfiguration/ResolvableConfiguration/DependencyScopeConfiguration"));
		}
	}

	private interface ConfigurationTypeRegistryFactory {
		NamedDomainObjectRegistry<Configuration> newRegistry(ConfigurationContainer container);
	}

	private static final class LegacyConfigurationFactory implements ConfigurationTypeRegistryFactory {
		private final Action<? super Configuration> configureAction;

		public LegacyConfigurationFactory() {
			this(ActionUtils.doNothing());
		}

		public LegacyConfigurationFactory(Action<? super Configuration> configureAction) {
			this.configureAction = configureAction;
		}

		@Override
		public NamedDomainObjectRegistry<Configuration> newRegistry(ConfigurationContainer container) {
			return new NamedDomainObjectRegistry<Configuration>() {
				@Override
				public NamedDomainObjectProvider<Configuration> register(String name) throws InvalidUserDataException {
					return container.register(name, configureAction);
				}

				@Override
				public NamedDomainObjectProvider<Configuration> registerIfAbsent(String name) {
					return NamedDomainObjectCollectionUtils.registerIfAbsent(container, name, configureAction);
				}

				@Override
				public RegistrableType getRegistrableType() {
					return Configuration.class::equals;
				}
			};
		}
	}

	private static final class FactoryMethodBasedRegistry implements ConfigurationTypeRegistryFactory {
		private final String configurationTypeName;
		private final Method factoryMethod;

		public FactoryMethodBasedRegistry(String configurationTypeName, Method factoryMethod) {
			this.configurationTypeName = configurationTypeName;
			this.factoryMethod = factoryMethod;
		}

		@Override
		public NamedDomainObjectRegistry<Configuration> newRegistry(ConfigurationContainer container) {
			return new NamedDomainObjectRegistry<Configuration>() {
				@Override
				public NamedDomainObjectProvider<Configuration> register(String name) throws InvalidUserDataException {
					create(name);
					return container.named(name);
				}

				@Override
				public NamedDomainObjectProvider<Configuration> registerIfAbsent(String name) {
					if (!container.getNames().contains(name)) {
						create(name);
					}
					return container.named(name);
				}

				private Configuration create(String name) {
					try {
						return (Configuration) factoryMethod.invoke(container, name);
					} catch (InvocationTargetException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public RegistrableType getRegistrableType() {
					return type -> type.getSimpleName().equals(configurationTypeName);
				}
			};
		}
	}

	private static Method tryFindCreateMethod(String methodName) {
		try {
			return ConfigurationContainer.class.getMethod(methodName, String.class);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	@Override
	public RegistrableTypes getRegistrableTypes() {
		return CONFIGURATION_REGISTRABLE_TYPES;
	}

	@Override
	public RegistrableType getRegistrableType() {
		return CONFIGURATION_REGISTRABLE_TYPES;
	}

	@Override
	public String toString() {
		return container + " registry";
	}

	private static final class DefaultRegistrableType implements RegistrableTypes, RegistrableType {
		@Override
		public boolean canRegisterType(Class<?> type) {
			Objects.requireNonNull(type);
			switch (type.getSimpleName()) {
				// Legacy Gradle
				case "Configuration":

					// Starting Gradle 8.4+
				case "ConsumableConfiguration":
				case "ResolvableConfiguration":
				case "DependencyScopeConfiguration":
					return true;
				default:
					return false;
			}
		}
	}
}
