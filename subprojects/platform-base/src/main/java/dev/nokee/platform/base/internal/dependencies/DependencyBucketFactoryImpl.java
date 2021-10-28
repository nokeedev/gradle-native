/*
 * Copyright 2020 the original author or authors.
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

import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.platform.base.DependencyBucket;
import lombok.val;
import org.gradle.api.artifacts.Configuration;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.mapDisplayName;
import static dev.nokee.utils.ConfigurationUtils.configureDescription;

public final class DependencyBucketFactoryImpl implements DependencyBucketFactory {
	private final NamedDomainObjectRegistry<Configuration> configurationRegistry;
	private final DependencyFactory dependencyFactory;

	public DependencyBucketFactoryImpl(NamedDomainObjectRegistry<Configuration> configurationRegistry, DependencyFactory dependencyFactory) {
		this.configurationRegistry = configurationRegistry;
		this.dependencyFactory = dependencyFactory;
	}

	@Override
	public DependencyBucket create(DependencyBucketIdentifier identifier) {
		val configurationProvider = configurationRegistry.registerIfAbsent(identifier.getConfigurationName());
		configurationProvider.configure(bucketTypeOf(identifier.getType())::configure);
		configurationProvider.configure(configureDescription(mapDisplayName(identifier)));

		return new DefaultDependencyBucket(identifier.getName().get(), configurationProvider, dependencyFactory);
	}

	private static ConfigurationBucketType bucketTypeOf(Class<?> type) {
		if (DeclarableDependencyBucket.class.isAssignableFrom(type)) {
			return ConfigurationBucketType.DECLARABLE;
		} else if (ConsumableDependencyBucket.class.isAssignableFrom(type)) {
			return ConfigurationBucketType.CONSUMABLE;
		} else if (ResolvableDependencyBucket.class.isAssignableFrom(type)) {
			return ConfigurationBucketType.RESOLVABLE;
		}
		throw new RuntimeException();
	}
}
