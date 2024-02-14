/*
 * Copyright 2024 the original author or authors.
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

package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectRegistry;
import org.gradle.api.artifacts.Configuration;

import javax.inject.Inject;

public final class ModernConfigurationFactory implements ConfigurationFactory {
	private static final Class<? extends Configuration> ConsumableConfiguration = loadClass("org.gradle.api.artifacts.ConsumableConfiguration");
	private static final Class<? extends Configuration> DependencyScopeConfiguration = loadClass("org.gradle.api.artifacts.DependencyScopeConfiguration");
	private static final Class<? extends Configuration> ResolvableConfiguration = loadClass("org.gradle.api.artifacts.ResolvableConfiguration");
	private final ModelObjectRegistry<Configuration> configurations;

	@Inject
	public ModernConfigurationFactory(ModelObjectRegistry<Configuration> configurations) {
		this.configurations = configurations;
	}

	@Override
	public Configuration newConsumable(ModelObjectIdentifier identifier) {
		return configurations.register(identifier, ConsumableConfiguration).get();
	}

	@Override
	public Configuration newDependencyScope(ModelObjectIdentifier identifier) {
		return configurations.register(identifier, DependencyScopeConfiguration).get();
	}

	@Override
	public Configuration newResolvable(ModelObjectIdentifier identifier) {
		return configurations.register(identifier, ResolvableConfiguration).get();
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Configuration> loadClass(String configurationClassName) {
		try {
			return (Class<? extends Configuration>) Class.forName(configurationClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
