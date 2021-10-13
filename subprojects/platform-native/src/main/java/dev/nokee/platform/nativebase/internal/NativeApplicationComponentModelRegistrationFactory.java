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
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.CalculateNativeApplicationVariantAction;
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
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.nativeApplicationProjection;

public final class NativeApplicationComponentModelRegistrationFactory {
	private final Class<? extends Component> componentType;
	private final BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration;
	private final Project project;

	public NativeApplicationComponentModelRegistrationFactory(Class<? extends Component> componentType, Project project, BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration) {
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
			.withComponent(createdUsing(of(DefaultNativeApplicationComponent.class), nativeApplicationProjection(name, project)))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

				sourceRegistration.accept(entity, path);

				val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultNativeApplicationComponent.class, ProjectIdentifier.of(project));
				val dependencyContainer = project.getObjects().newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.getConfigurations()), project.getDependencies())));
				val dependencies = project.getObjects().newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("dependencies"))
					.withComponent(IsModelProperty.tag())
					.withComponent(ModelProjections.ofInstance(dependencies))
					.build());

				// TODO: Should be created as ModelProperty (readonly) with VariantView<NativeApplication> projection
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("variants"))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(of(VariantView.class), () -> new VariantViewAdapter<>(new ViewAdapter<>(NativeApplication.class, new ModelNodeBackedViewStrategy(project.getProviders(), () -> ModelStates.finalize(entity))))))
					.build());

				// TODO: Should be created as ModelProperty (readonly) with BinaryView<Binary> projection
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("binaries"))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(of(BinaryView.class), () -> new BinaryViewAdapter<>(new ViewAdapter<>(Binary.class, new ModelNodeBackedViewStrategy(project.getProviders(), () -> ModelStates.finalize(entity))))))
					.build());
			})))
			.action(self(stateOf(ModelState.Finalized)).apply(new CalculateNativeApplicationVariantAction(project)))
			;
	}
}
