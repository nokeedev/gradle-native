/*
 * Copyright 2022 the original author or authors.
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

import com.google.common.reflect.TypeToken;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.createdUsingNoInject;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.dependencyBuckets;
import static dev.nokee.utils.ConfigurationUtils.configureAsConsumable;
import static dev.nokee.utils.ConfigurationUtils.configureAsDeclarable;
import static dev.nokee.utils.ConfigurationUtils.configureAsResolvable;

public abstract class DependencyBucketCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private final NamedDomainObjectRegistry<Configuration> registry;
	private final ConfigurationContainer configurations;
	private final DependencyFactory factory;

	@Inject
	public DependencyBucketCapabilityPlugin(ConfigurationContainer configurations, DependencyHandler dependencies) {
		this.registry = NamedDomainObjectRegistry.of(configurations);
		this.configurations = configurations;
		this.factory = DependencyFactory.forHandler(dependencies);
	}

	@Override
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsDependencyBucket.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), this::createConfiguration));

		dependencyBuckets(target).configureEach(new DescriptionRule());

		dependencyBuckets(target).withType(ConsumableDependencyBucketSpec.class).configureEach(it -> {
			configureAsConsumable().execute(it.getAsConfiguration());
		});
		dependencyBuckets(target).withType(ResolvableDependencyBucketSpec.class).configureEach(it -> {
			configureAsResolvable().execute(it.getAsConfiguration());
		});
		dependencyBuckets(target).withType(DeclarableDependencyBucketSpec.class).configureEach(it -> {
			configureAsDeclarable().execute(it.getAsConfiguration());
		});

		configurations.configureEach(configuration -> {
			val bucketResolver = (Runnable) new Runnable() {
				private boolean locking = false;
				private boolean alreadyExecuted = false;

				@Override
				public void run() {
					if (!alreadyExecuted) {
						assert !locking;
						locking = true;
						finalize(configuration);
						alreadyExecuted = true;
					}
				}

				private /*static*/ void finalize(Configuration configuration) {
					val projections = target.getExtensions().getByType(ModelLookup.class).query(entity -> entity.hasComponent(typeOf(IsDependencyBucket.class)) && entity.find(ConfigurationComponent.class).map(it -> it.get().getName()).map(configuration.getName()::equals).orElse(false));
					projections.forEach(ModelStates::finalize);
					for (Configuration config : configuration.getExtendsFrom()) {
						finalize(config);
					}
				}
			};
			configuration.defaultDependencies(__ -> bucketResolver.run());
			((ConfigurationInternal) configuration).beforeLocking(__ -> bucketResolver.run());
		});
	}

	private static final class DescriptionRule implements Action<DependencyBucket> {
		@Override
		public void execute(DependencyBucket dependencyBucket) {
			if (dependencyBucket instanceof ModelElement) {
				final ModelObjectIdentifier identifier = ((ModelElement) dependencyBucket).getIdentifier();

				DisplayName displayName = null;
				if (dependencyBucket instanceof DeclarableDependencyBucket) {
					displayName = DisplayName.of(DependencyBuckets.defaultDisplayNameOfDeclarableBucket(identifier.getName()));
				} else {
					displayName = DisplayName.of(DependencyBuckets.defaultDisplayName(identifier.getName()));
				}

				dependencyBucket.getAsConfiguration().setDescription(DependencyBucketDescription.of(displayName).forOwner(dependencyBucket.toString()).toString());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void createConfiguration(ModelNode entity, ModelComponentTag<IsDependencyBucket> ignored, FullyQualifiedNameComponent fullyQualifiedName) {
		val configurationProvider = registry.registerIfAbsent(fullyQualifiedName.get().toString());
		entity.addComponent(new ConfigurationComponent(configurationProvider));
		entity.addComponent(new ModelElementProviderSourceComponent(configurationProvider));
		entity.addComponent(createdUsing((ModelType<NamedDomainObjectProvider<Configuration>>) ModelType.of(new TypeToken<NamedDomainObjectProvider<Configuration>>() {}.getType()), () -> configurationProvider));
		entity.addComponent(createdUsingNoInject(of(Configuration.class), configurationProvider::get));
	}
}
