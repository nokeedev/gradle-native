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

import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.DefaultDomainObjectIdentifier;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.createdUsingNoInject;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ConfigurationUtils.configureAsConsumable;
import static dev.nokee.utils.ConfigurationUtils.configureAsDeclarable;
import static dev.nokee.utils.ConfigurationUtils.configureAsResolvable;
import static dev.nokee.utils.ConfigurationUtils.configureDescription;
import static dev.nokee.utils.Optionals.ifPresentOrElse;

public abstract class DependencyBucketCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private final NamedDomainObjectRegistry<Configuration> registry;

	@Inject
	public DependencyBucketCapabilityPlugin(ConfigurationContainer configurations) {
		this.registry = NamedDomainObjectRegistry.of(configurations);
	}

	@Override
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsDependencyBucket.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), this::createConfiguration));

		target.getExtensions().getByType(ModelConfigurer.class).configure(new DisplayNameRule());

		// ComponentFromEntity<ParentComponent> read-only self
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsDependencyBucket.class), ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(DisplayNameComponent.class), ModelComponentReference.of(ElementNameComponent.class), (entity, ignored1, path, displayName, elementName) -> {
			val parentIdentifier = entity.find(ParentComponent.class).map(parent -> parent.get().get(IdentifierComponent.class).get()).orElse(null);
			entity.addComponent(new IdentifierComponent(new DefaultDomainObjectIdentifier(elementName.get(), parentIdentifier, displayName.get(), path.get())));
		}));

		target.getExtensions().getByType(ModelConfigurer.class).configure(new DescriptionRule());

		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ConsumableDependencyBucketTag.class), ModelComponentReference.of(ConfigurationComponent.class), (entity, ignored1, configuration) -> {
			configuration.configure(configureAsConsumable());
		}));
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ResolvableDependencyBucketTag.class), ModelComponentReference.of(ConfigurationComponent.class), (entity, ignored1, configuration) -> {
			configuration.configure(configureAsResolvable());
		}));
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(DeclarableDependencyBucketTag.class), ModelComponentReference.of(ConfigurationComponent.class), (entity, ignored1, configuration) -> {
			configuration.configure(configureAsDeclarable());
		}));
	}

	// ComponentFromEntity<DisplayNameComponent> read/write self
	// ComponentFromEntity<DeclarableDependencyBucketSpec.Tag> read-only self
	private static final class DisplayNameRule extends ModelActionWithInputs.ModelAction3<ModelComponentTag<IsDependencyBucket>, ElementNameComponent, ModelState.IsAtLeastCreated> {

		public DisplayNameRule() {
			super(ModelTags.referenceOf(IsDependencyBucket.class), ModelComponentReference.of(ElementNameComponent.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class));
		}

		@Override
		protected void execute(ModelNode entity, ModelComponentTag<IsDependencyBucket> ignored1, ElementNameComponent elementName, ModelState.IsAtLeastCreated ignored2) {
			if (!entity.has(DisplayNameComponent.class)) {
				if (entity.hasComponent(ModelTags.typeOf(DeclarableDependencyBucketTag.class))) {
					entity.addComponent(new DisplayNameComponent(DependencyBuckets.defaultDisplayNameOfDeclarableBucket(elementName.get())));
				} else {
					entity.addComponent(new DisplayNameComponent(DependencyBuckets.defaultDisplayName(elementName.get())));
				}
			}
		}
	}

	// ComponentFromEntity<ParentComponent> read-only self
	// ComponentFromEntity<IdentifierComponent> read-only parent
	private static final class DescriptionRule extends ModelActionWithInputs.ModelAction4<ModelComponentTag<IsDependencyBucket>, ConfigurationComponent, ParentComponent, DisplayNameComponent> {

		public DescriptionRule() {
			super(ModelTags.referenceOf(IsDependencyBucket.class), ModelComponentReference.of(ConfigurationComponent.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(DisplayNameComponent.class));
		}

		@Override
		protected void execute(ModelNode entity, ModelComponentTag<IsDependencyBucket> ignored, ConfigurationComponent configuration, ParentComponent parent, DisplayNameComponent displayName) {
			ifPresentOrElse(entity.find(ParentComponent.class).flatMap(it -> it.get().find(IdentifierComponent.class))
					.map(IdentifierComponent::get),
				it -> configuration.configure(configureDescription(DependencyBucketDescription.of(displayName.get()).forOwner(it)::toString)),
				() -> configuration.configure(configureDescription(DependencyBucketDescription.of(displayName.get())::toString)));
		}
	}

	private void createConfiguration(ModelNode entity, ModelComponentTag<IsDependencyBucket> ignored, FullyQualifiedNameComponent fullyQualifiedName) {
		val configurationProvider = registry.registerIfAbsent(fullyQualifiedName.get().toString());
		entity.addComponent(new ConfigurationComponent(configurationProvider));
		entity.addComponent(new ModelElementProviderSourceComponent(configurationProvider));
		entity.addComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> configurationProvider));
		entity.addComponent(createdUsingNoInject(of(Configuration.class), configurationProvider::get));
	}
}
