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
import dev.nokee.model.capabilities.variants.LinkedVariantsComponent;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariantComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.ExtendsFromParentConfigurationAction;
import dev.nokee.platform.base.internal.dependencybuckets.CompileOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.developmentbinary.DevelopmentBinaryPropertyComponent;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultIosApplicationVariant;
import dev.nokee.platform.ios.internal.IosApplicationComponentTag;
import dev.nokee.platform.ios.internal.IosApplicationOutgoingDependencies;
import dev.nokee.platform.ios.internal.IosResourceSetSpec;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.NativeVariantTag;
import dev.nokee.platform.nativebase.internal.TargetBuildTypesPropertyComponent;
import dev.nokee.platform.nativebase.internal.TargetLinkagesPropertyComponent;
import dev.nokee.platform.nativebase.internal.TargetMachinesPropertyComponent;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketTag;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependenciesComponent;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.util.Collections;

import static dev.nokee.model.internal.actions.ModelAction.configureMatching;
import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;

public class IosComponentBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(NativeComponentBasePlugin.class);

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(IosApplicationComponentTag.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), (entity, identifier, tag, fullyQualifiedName) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(newEntity("resources", IosResourceSetSpec.class, it -> it.ownedBy(entity))).configure(IosResourceSet.class, sourceSet -> sourceSet.from("src/" + fullyQualifiedName.get() + "/resources"));

			val implementation = registry.register(newEntity("implementation", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val compileOnly = registry.register(newEntity("compileOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val linkOnly = registry.register(newEntity("linkOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val runtimeOnly = registry.register(newEntity("runtimeOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));

			entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
			entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
			entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
			entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeVariantTag.class), ModelComponentReference.of(ParentComponent.class), (entity, identifier, tag, parent) -> {
			if (!parent.get().hasComponent(typeOf(IosApplicationComponentTag.class))) {
				return;
			}

			val registry = project.getExtensions().getByType(ModelRegistry.class);

			val implementation = registry.register(newEntity("implementation", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val compileOnly = registry.register(newEntity("compileOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val linkOnly = registry.register(newEntity("linkOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));
			val runtimeOnly = registry.register(newEntity("runtimeOnly", DeclarableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(FrameworkAwareDependencyBucketTag.class)));

			entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
			entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
			entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
			entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

			val runtimeElements = registry.register(newEntity("runtimeElements", ConsumableDependencyBucketSpec.class, it -> it.ownedBy(entity)));
			runtimeElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
				.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
				.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) ((VariantIdentifier) identifier.get()).getBuildVariant(), project.getObjects())));
			val outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new IosApplicationOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
			entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(ModelProperties.getProperty(entity, "dependencies").as(NativeComponentDependencies.class)::get, outgoing.get()));

			registry.instantiate(configureMatching(ownedBy(entity.getId()).and(subtypeOf(of(Configuration.class))), new ExtendsFromParentConfigurationAction()));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<ModelComponentTag<IosApplicationComponentTag>, LinkedVariantsComponent>() {
			protected void execute(ModelNode entity, ModelComponentTag<IosApplicationComponentTag> tag, LinkedVariantsComponent variants) {
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
			}
		});

		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<ModelComponentTag<IosApplicationComponentTag>, TargetMachinesPropertyComponent>() {
			protected void execute(ModelNode entity, ModelComponentTag<IosApplicationComponentTag> tag, TargetMachinesPropertyComponent targetMachines) {
				((SetProperty<TargetMachine>) targetMachines.get().get(GradlePropertyComponent.class).get()).convention(Collections.singletonList(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64()));
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<ModelComponentTag<IosApplicationComponentTag>, TargetLinkagesPropertyComponent>() {
			protected void execute(ModelNode entity, ModelComponentTag<IosApplicationComponentTag> tag, TargetLinkagesPropertyComponent targetLinkages) {
				((SetProperty<TargetLinkage>) targetLinkages.get().get(GradlePropertyComponent.class).get()).convention(Collections.singletonList(TargetLinkages.EXECUTABLE));
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<ModelComponentTag<IosApplicationComponentTag>, TargetBuildTypesPropertyComponent>() {
			protected void execute(ModelNode entity, ModelComponentTag<IosApplicationComponentTag> tag, TargetBuildTypesPropertyComponent targetBuildTypes) {
				((SetProperty<TargetBuildType>) targetBuildTypes.get().get(GradlePropertyComponent.class).get()).convention(Collections.singletonList(TargetBuildTypes.named("Default")));
			}
		});
	}

	@SuppressWarnings("unchecked")
	private static void onEachVariantDependencies(ModelNode variant, VariantComponentDependencies<?> dependencies, ProviderFactory providers) {
		dependencies.getOutgoing().getExportedBinary().convention(providers.provider(() -> (Provider<Binary>) ModelStates.finalize(variant).get(DevelopmentBinaryPropertyComponent.class).get().get(GradlePropertyComponent.class).get()).flatMap(it -> it));
	}

	private static ModelRegistration iosApplicationVariant(VariantIdentifier identifier, DefaultIosApplicationComponent component, Project project) {
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(NativeVariantTag.class)
			.mergeFrom(tagsOf(DefaultIosApplicationVariant.class))
			.withComponent(createdUsing(of(DefaultIosApplicationVariant.class), () -> {
				val variant = project.getObjects().newInstance(DefaultIosApplicationVariant.class);
				variant.getProductBundleIdentifier().convention(component.getGroupId().map(it -> it + "." + component.getModuleName().get()));
				return variant;
			}))
			.build()
			;
	}
}
