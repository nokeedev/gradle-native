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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.utils.ConfigurationUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.plugins.ExtensionAware;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.createdUsingNoInject;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.configurationName;
import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.toDescription;

public final class DeclarableDependencyBucketRegistrationFactory {
	private final NamedDomainObjectRegistry<Configuration> configurationRegistry;
	private final DependencyBucketFactory bucketFactory;

	public DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry<Configuration> configurationRegistry, DependencyBucketFactory bucketFactory) {
		this.configurationRegistry = configurationRegistry;
		this.bucketFactory = bucketFactory;
	}

	public ModelRegistration create(DependencyBucketIdentifier identifier) {
		val bucket = new DefaultDeclarableDependencyBucket(bucketFactory.create(identifier));
		val configurationProvider = configurationRegistry.registerIfAbsent(configurationName(identifier));
		configurationProvider.configure(ConfigurationUtils.configureAsDeclarable());
		configurationProvider.configure(ConfigurationUtils.configureDescription(toDescription(identifier)));
		configurationProvider.configure(configuration -> {
			val extension = ((ExtensionAware) configuration).getExtensions().findByName("__bucket");
			if (extension == null) {
				((ExtensionAware) configuration).getExtensions().add(DeclarableDependencyBucket.class, "__bucket", bucket);
			} else if (!(extension instanceof DeclarableDependencyBucket)) {
				throw new IllegalStateException("Bucket registration mismatch!");
			}
		});
		val entityPath = toPath(identifier);
		return ModelRegistration.builder()
			.withComponent(entityPath)
			.withComponent(identifier)
			.withComponent(IsDependencyBucket.tag())
			.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> configurationProvider))
			.withComponent(createdUsingNoInject(of(Configuration.class), configurationProvider::get))
			.withComponent(createdUsing(of(DeclarableDependencyBucket.class), () -> {
				return ((ExtensionAware) configurationProvider.get()).getExtensions().getByType(DeclarableDependencyBucket.class);
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (entity, path, ignored) -> {
				if (entityPath.equals(path)) {
					configurationProvider.configure(configuration -> {
						((ConfigurationInternal) configuration).beforeLocking(it -> ModelStates.realize(entity));
					});
				}
			}))
			.build();
	}

	private static final class DefaultDeclarableDependencyBucket implements DeclarableDependencyBucket {
		private final DependencyBucket delegate;

		private DefaultDeclarableDependencyBucket(DependencyBucket delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public void addDependency(Object notation) {
			delegate.addDependency(notation);
		}

		@Override
		public void addDependency(Object notation, Action<? super ModuleDependency> action) {
			delegate.addDependency(notation, action);
		}

		@Override
		public Configuration getAsConfiguration() {
			return delegate.getAsConfiguration();
		}
	}
}
