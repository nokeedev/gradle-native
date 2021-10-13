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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.utils.TransformerUtils;
import lombok.val;
import org.gradle.api.Project;

import java.util.function.BiConsumer;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.maven;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.withConventionOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.nativeLibraryProjection;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;

public final class NativeLibraryComponentModelRegistrationFactory {
	private final Class<? extends Component> componentType;
	private final BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration;
	private final Project project;

	public NativeLibraryComponentModelRegistrationFactory(Class<? extends Component> componentType, Project project, BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration) {
		this.componentType = componentType;
		this.sourceRegistration = sourceRegistration;
		this.project = project;
	}

	public NodeRegistration create(String name) {
		return NodeRegistration.of(name, of(componentType))
			// TODO: Should configure FileCollection on CApplication
			//   and link FileCollection to source sets
			.action(allDirectDescendants(mutate(of(LanguageSourceSet.class)))
				.apply(executeUsingProjection(of(LanguageSourceSet.class), withConventionOf(maven(ComponentName.of(name)))::accept)))
			.withComponent(IsComponent.tag())
			.withComponent(createdUsing(of(NativeLibraryComponentVariants.class), () -> {
				val component = ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), DefaultNativeLibraryComponent.class);
				return new NativeLibraryComponentVariants(project.getObjects(), component, project.getDependencies(), project.getConfigurations(), project.getProviders(), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(ModelLookup.class));
			}))
			.withComponent(createdUsing(of(DefaultNativeLibraryComponent.class), nativeLibraryProjection(name, project)))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);

				sourceRegistration.accept(entity, path);

				// TODO: Should be created as ModelProperty (readonly) with VariantView<NativeLibrary> projection
				val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultNativeLibraryComponent.class, ProjectIdentifier.of(project));
				val dependencyContainer = project.getObjects().newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.getConfigurations()), project.getDependencies())));
				val dependencies = project.getObjects().newInstance(DefaultNativeLibraryComponentDependencies.class, dependencyContainer);
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("dependencies"))
					.withComponent(IsModelProperty.tag())
					.withComponent(ModelProjections.ofInstance(dependencies))
					.build());

				// TODO: Should be created as ModelProperty (readonly) with VariantView<NativeLibrary> projection
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("variants"))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(of(VariantView.class), () -> new VariantViewAdapter<>(new ViewAdapter<>(NativeLibrary.class, new ModelNodeBackedViewStrategy(project.getProviders(), () -> ModelStates.finalize(entity))))))
					.build());

				// TODO: Should be created as ModelProperty (readonly) with BinaryView<Binary> projection
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("binaries"))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(of(BinaryView.class), () -> {
						return ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), DefaultNativeLibraryComponent.class).getBinaries();
					}))
					.build());
			})))
			.action(self(stateOf(ModelState.Finalized)).apply(new CalculateNativeApplicationVariantAction(project)))
			;
	}

	private static class CalculateNativeApplicationVariantAction extends ModelActionWithInputs.ModelAction1<ModelPath> {
		private final Project project;

		private CalculateNativeApplicationVariantAction(Project project) {
			this.project = project;
		}

		@Override
		protected void execute(ModelNode entity, ModelPath path) {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

			val component = ModelNodeUtils.get(entity, ModelType.of(DefaultNativeLibraryComponent.class));
			component.finalizeExtension(null);

			component.getVariantCollection().whenElementKnown(knownVariant -> {
				val variant = registry.register(ModelRegistration.builder()
					.withComponent(path.child(knownVariant.getIdentifier().getUnambiguousName()))
					.withComponent(IsVariant.tag())
					.withComponent(createdUsing(ModelType.of(NativeLibrary.class), () -> knownVariant.map(noOpTransformer()).get()))
					.build());
				knownVariant.configure(it -> ModelStates.realize(ModelNodes.of(variant)));

				registry.register(propertyFactory.create(path.child("variants").child(knownVariant.getIdentifier().getUnambiguousName()), ModelNodes.of(variant)));
			});
		}
	}
}
