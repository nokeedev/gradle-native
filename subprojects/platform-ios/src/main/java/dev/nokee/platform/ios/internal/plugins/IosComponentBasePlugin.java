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
package dev.nokee.platform.ios.internal.plugins;

import com.google.common.collect.Streams;
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.nativebase.internal.HasRuntimeElementsDependencyBucket;
import dev.nokee.model.capabilities.variants.LinkedVariantsComponent;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.internal.BuildVariantComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultIosApplicationVariant;
import dev.nokee.platform.ios.internal.IosApplicationComponentTag;
import dev.nokee.platform.ios.internal.IosApplicationOutgoingDependencies;
import dev.nokee.platform.ios.internal.IosResourceSetSpec;
import dev.nokee.platform.ios.internal.rules.IosDevelopmentBinaryConvention;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.NativeVariantTag;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependenciesComponent;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.TypeOf;

import java.util.Collections;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;

public class IosComponentBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(NativeComponentBasePlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(IosResourceSetSpec.class, new ModelObjectFactory<IosResourceSetSpec>(project, IsLanguageSourceSet.class) {
			@Override
			protected IosResourceSetSpec doCreate(String name) {
				return project.getObjects().newInstance(IosResourceSetSpec.class);
			}
		});

		variants(project).withType(DefaultIosApplicationVariant.class).configureEach(variant -> {
			variant.getDevelopmentBinary().convention(variant.getBinaries().getElements().flatMap(IosDevelopmentBinaryConvention.INSTANCE));
		});

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(IosApplicationComponentTag.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), (entity, identifier, tag, fullyQualifiedName) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(newEntity(identifier.get().child("resources"), IosResourceSetSpec.class, it -> it.ownedBy(entity))).configure(IosResourceSet.class, sourceSet -> sourceSet.from("src/" + fullyQualifiedName.get() + "/resources"));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeVariantTag.class), ModelComponentReference.of(ParentComponent.class), ModelComponentReference.ofProjection(HasRuntimeElementsDependencyBucket.class), (entity, identifier, tag, parent, ignored) -> {
			if (!parent.get().hasComponent(typeOf(IosApplicationComponentTag.class))) {
				return;
			}

			val outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new IosApplicationOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(entity), HasRuntimeElementsDependencyBucket.class).getRuntimeElements().getAsConfiguration(), project.getObjects())));
			entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(() -> (NativeComponentDependencies) ModelNodeUtils.get(entity, DependencyAwareComponent.class).getDependencies(), outgoing.get()));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IosApplicationComponentTag.class), ModelComponentReference.of(LinkedVariantsComponent.class), (entity, tag, variants) -> {
			val component = ModelNodeUtils.get(entity, DefaultIosApplicationComponent.class);

			Streams.zip(component.getBuildVariants().get().stream(), Streams.stream(variants), (buildVariant, variant) -> {
				val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();

				iosApplicationVariant(variantIdentifier, component, project).getComponents().forEach(variant::addComponent);
				variant.addComponent(new BuildVariantComponent(buildVariant));
				ModelStates.register(variant);

				onEachVariantDependencies(variant, variant.getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)), project.getProviders());
				return null;
			}).forEach(it -> {});

			component.finalizeValue();
		}));

		components(project).withType(ObjectiveCIosApplicationPlugin.DefaultObjectiveCIosApplication.class).configureEach(component -> {
			component.getTargetMachines().convention(Collections.singletonList(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64()));
		});
		components(project).withType(SwiftIosApplicationPlugin.DefaultSwiftIosApplication.class).configureEach(component -> {
			component.getTargetMachines().convention(Collections.singletonList(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64()));
		});
		components(project).withType(ObjectiveCIosApplicationPlugin.DefaultObjectiveCIosApplication.class).configureEach(component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		});
		components(project).withType(SwiftIosApplicationPlugin.DefaultSwiftIosApplication.class).configureEach(component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
		});
		components(project).withType(ObjectiveCIosApplicationPlugin.DefaultObjectiveCIosApplication.class).configureEach(component -> {
			component.getTargetBuildTypes().convention(Collections.singletonList(TargetBuildTypes.named("Default")));
		});
		components(project).withType(SwiftIosApplicationPlugin.DefaultSwiftIosApplication.class).configureEach(component -> {
			component.getTargetBuildTypes().convention(Collections.singletonList(TargetBuildTypes.named("Default")));
		});
	}

	private static void onEachVariantDependencies(ModelNode variant, VariantComponentDependencies<?> dependencies, ProviderFactory providers) {
		dependencies.getOutgoing().getExportedBinary().convention(providers.provider(() -> ModelNodeUtils.get(ModelStates.finalize(variant), HasDevelopmentBinary.class).getDevelopmentBinary()).flatMap(it -> it));
	}

	private static ModelRegistration iosApplicationVariant(VariantIdentifier identifier, DefaultIosApplicationComponent component, Project project) {
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(NativeVariantTag.class)
			.mergeFrom(tagsOf(DefaultIosApplicationVariant.class))
			.withComponent(createdUsing(of(DefaultIosApplicationVariant.class), () -> {
				val variant = project.getObjects().newInstance(DefaultIosApplicationVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), (Factory<ComponentSources>) () -> project.getObjects().newInstance(ComponentSources.class));
				variant.getProductBundleIdentifier().convention(component.getGroupId().map(it -> it + "." + component.getModuleName().get()));
				return variant;
			}))
			.build()
			;
	}
}
