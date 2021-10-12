/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.objectivec.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.BaseDomainObjectViewProjection;
import dev.nokee.model.internal.BaseNamedDomainObjectViewProjection;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.objectivec.ObjectiveCApplication;
import dev.nokee.platform.objectivec.ObjectiveCApplicationSources;
import dev.nokee.utils.TransformerUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import java.util.function.Consumer;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.nativebase.internal.plugins.NativeApplicationPlugin.nativeApplicationVariant;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;

public class ObjectiveCApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
		ModelNodeUtils.get(ModelNodes.of(components), NodeRegistrationFactoryRegistry.class).registerFactory(of(ObjectiveCApplication.class), name -> objectiveCApplication(name, project));
		val componentProvider = components.register("main", ObjectiveCApplication.class, configureUsingProjection(DefaultNativeApplicationComponent.class, baseNameConvention(project.getName()).andThen(configureBuildVariants())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(ObjectiveCApplication.class, EXTENSION_NAME, extension);
	}

	public static NodeRegistration objectiveCApplication(String name, Project project) {
		return NodeRegistration.of(name, of(ObjectiveCApplication.class))
			// TODO: Should configure FileCollection on CApplication
			//   and link FileCollection to source sets
			.action(allDirectDescendants(mutate(of(LanguageSourceSet.class)))
				.apply(executeUsingProjection(of(LanguageSourceSet.class), withConventionOf(maven(ComponentName.of(name)), defaultObjectiveCGradle(ComponentName.of(name)))::accept)))
			.withComponent(IsComponent.tag())
			.withComponent(createdUsing(of(DefaultNativeApplicationComponent.class), nativeApplicationProjection(name, project)))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

				// TODO: Should be created using ObjectiveCSourceSetSpec
				val objectiveC = registry.register(ModelRegistration.builder()
					.withComponent(path.child("objectiveC"))
					.withComponent(managed(of(CSourceSet.class)))
					.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created using CHeaderSetSpec
				val headers = registry.register(ModelRegistration.builder()
					.withComponent(path.child("headers"))
					.withComponent(managed(of(CHeaderSet.class)))
					.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
					.build());

				// TODO: Should be created as ModelProperty (readonly) with ObjectiveCApplicationSources projection
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("sources"))
					.withComponent(IsModelProperty.tag())
					.withComponent(managed(of(ObjectiveCApplicationSources.class)))
					.withComponent(managed(of(BaseDomainObjectViewProjection.class)))
					.withComponent(managed(of(BaseNamedDomainObjectViewProjection.class)))
					.build());

				registry.register(propertyFactory.create(path.child("sources").child("objectiveC"), ModelNodes.of(objectiveC)));
				registry.register(propertyFactory.create(path.child("sources").child("headers"), ModelNodes.of(headers)));

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
					.withComponent(createdUsing(of(VariantView.class), () -> new VariantViewAdapter<>(new ViewAdapter<>(NativeApplication.class, new ModelNodeBackedViewStrategy(project.getProviders())))))
					.build());

				// TODO: Should be created as ModelProperty (readonly) with BinaryView<Binary> projection
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("binaries"))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(of(BinaryView.class), () -> new BinaryViewAdapter<>(new ViewAdapter<>(Binary.class, new ModelNodeBackedViewStrategy(project.getProviders(), () -> ModelStates.finalize(entity))))))
					.build());
			})))
			.action(self(stateOf(ModelState.Finalized)).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

				val component = ModelNodeUtils.get(entity, of(DefaultNativeApplicationComponent.class));
				component.finalizeExtension(null);
				component.getDevelopmentVariant().set(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get()))); // TODO: VariantView#get should force finalize the component.

				component.getBuildVariants().get().forEach(new Consumer<BuildVariantInternal>() {
					@Override
					public void accept(BuildVariantInternal buildVariant) {
						val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultNativeApplicationVariant.class).build();
						val variant = ModelNodeUtils.register(entity, nativeApplicationVariant(variantIdentifier, component, project));

						onEachVariantDependencies(variant.as(NativeApplication.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));

						registry.register(propertyFactory.create(path.child("variants").child(variantIdentifier.getUnambiguousName()), ModelNodes.of(variant)));
					}

					private void onEachVariantDependencies(DomainObjectProvider<NativeApplication> variant, VariantComponentDependencies<?> dependencies) {
						dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
					}
				});
			})))
			;
	}
}
