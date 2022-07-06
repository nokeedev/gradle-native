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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.DefaultDomainObjectIdentifier;
import dev.nokee.model.internal.buffers.ModelBuffers;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedName;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.createdUsingNoInject;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ConfigurationUtils.configureAsConsumable;
import static dev.nokee.utils.ConfigurationUtils.configureAsDeclarable;
import static dev.nokee.utils.ConfigurationUtils.configureAsResolvable;
import static dev.nokee.utils.ConfigurationUtils.configureDependencies;
import static dev.nokee.utils.ConfigurationUtils.configureDescription;
import static dev.nokee.utils.Optionals.ifPresentOrElse;

public abstract class DependencyBucketCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private final NamedDomainObjectRegistry<Configuration> registry;
	private final ObjectFactory objects;
	private final ProviderFactory providers;
	private final DependencyFactory factory;

	@Inject
	public DependencyBucketCapabilityPlugin(ConfigurationContainer configurations, ObjectFactory objects, ProviderFactory providers, DependencyHandler dependencies) {
		this.registry = NamedDomainObjectRegistry.of(configurations);
		this.objects = objects;
		this.providers = providers;
		this.factory = DependencyFactory.forHandler(dependencies);
	}

	@Override
	public void apply(T target) {
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsDependencyBucket.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), this::createConfiguration));

		target.getExtensions().getByType(ModelConfigurer.class).configure(new DisplayNameRule());

		// ComponentFromEntity<IsDependencyBucket> read-only
		// ComponentFromEntity<FullyQualifiedNameComponent> read-only
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsDependencyBucket.class), ModelComponentReference.of(ModelStates.Finalizing.class), (entity, ignored1, ignored2) -> {
			val fullyQualifiedNames = ModelNodeUtils.get(entity, Configuration.class).getExtendsFrom().stream().map(Configuration::getName).map(FullyQualifiedName::of).collect(ImmutableList.toImmutableList());
			target.getExtensions().getByType(ModelLookup.class).query(e -> e.hasComponent(ModelTags.typeOf(IsDependencyBucket.class)) && e.find(FullyQualifiedNameComponent.class).map(FullyQualifiedNameComponent::get).map(fullyQualifiedNames::contains).orElse(false)).forEach(ModelStates::finalize);
		}));

		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IsDependencyBucket.class), (entity, ignored) -> {
			entity.addComponent(ModelBuffers.empty(DependencyElement.class));
		}));

		target.getExtensions().getByType(ModelConfigurer.class).configure(new AttachDependenciesRule(factory));

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

		target.getExtensions().getByType(ModelConfigurer.class).configure(new AttachOutgoingArtifactRule());

		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ConsumableDependencyBucketTag.class), (entity, ignored1) -> {
			val outgoing = objects.newInstance(OutgoingArtifacts.class);
			entity.addComponent(ofInstance(outgoing));
		}));
		target.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ResolvableDependencyBucketTag.class), ModelComponentReference.of(ConfigurationComponent.class), (entity, ignored, configuration) -> {
			val incoming = new IncomingArtifacts(configuration.configuration);
			entity.addComponent(ofInstance(incoming));
		}));
	}

	// We have to delay until realize because Kotlin plugin suck big time.
	private static final class AttachDependenciesRule extends ModelActionWithInputs.ModelAction3<ModelComponentTag<IsDependencyBucket>, ConfigurationComponent, ModelState.IsAtLeastFinalized> {
		private final DependencyFactory factory;

		AttachDependenciesRule(DependencyFactory factory) {
			super(ModelTags.referenceOf(IsDependencyBucket.class), ModelComponentReference.of(ConfigurationComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class));
			this.factory = factory;
		}

		@Override
		public void execute(ModelNode entity, ModelComponentTag<IsDependencyBucket> ignored1, ConfigurationComponent configuration, ModelState.IsAtLeastFinalized ignored2) {
			configuration.configure(configureDependencies((self, set) -> {
				// There is a strange conflict where using addAllLater here result in an unrelated exception
				set.addAll(Streams.stream(entity.getComponent(ModelBuffers.typeOf(DependencyElement.class))).map(it -> it.resolve(new DependencyFactory() {
					private final Action<ModuleDependency> action = defaultAction(entity);

					@Override
					public Dependency create(Object notation) {
						val result = toDependency(notation);
						action.execute((ModuleDependency) result);
						return result;
					}

					private Dependency toDependency(Object notation) {
						if (notation instanceof Provider) {
							return factory.create(((Provider<?>) notation).get());
						} else {
							return factory.create(notation);
						}
					}
				})).collect(ImmutableList.toImmutableList()));
			}));
		}

		private static ActionUtils.Action<ModuleDependency> defaultAction(ModelNode entity) {
			assert entity.hasComponent(ModelTags.typeOf(IsDependencyBucket.class));
			return entity.find(DependencyDefaultActionComponent.class).map(DependencyDefaultActionComponent::get)
				.map(ActionUtils.Action::of).orElse(ActionUtils.doNothing());
		}
	}

	private static final class AttachOutgoingArtifactRule extends ModelActionWithInputs.ModelAction4<ModelComponentTag<ConsumableDependencyBucketTag>, ModelProjection, ConfigurationComponent, ModelState.IsAtLeastRealized> {
		public AttachOutgoingArtifactRule() {
			super(ModelTags.referenceOf(ConsumableDependencyBucketTag.class), ModelComponentReference.ofProjection(OutgoingArtifacts.class), ModelComponentReference.of(ConfigurationComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class));
		}

		@Override
		protected void execute(ModelNode entity, ModelComponentTag<ConsumableDependencyBucketTag> ignored1, ModelProjection ignored2, ConfigurationComponent configuration, ModelState.IsAtLeastRealized ignored3) {
			configuration.configure(attachOutgoingArtifactToConfiguration(ModelNodeUtils.get(entity, OutgoingArtifacts.class)));
		}

		private static ActionUtils.Action<Configuration> attachOutgoingArtifactToConfiguration(OutgoingArtifacts outgoing) {
			return configuration -> {
				configuration.getOutgoing().getArtifacts().addAllLater(outgoing.getArtifacts());
			};
		}
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
