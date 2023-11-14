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
import dev.nokee.model.capabilities.variants.LinkedVariantsComponent;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
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
import dev.nokee.platform.nativebase.internal.NativeVariantTag;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.internal.rules.ToBinariesCompileTasksTransformer;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.reflect.TypeOf;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.utils.TaskUtils.configureDependsOn;

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
		variants(project).withType(DefaultIosApplicationVariant.class).configureEach(variant -> {
			variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
		});
		variants(project).withType(DefaultIosApplicationVariant.class).configureEach(variant -> {
			variant.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
		});

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(IosApplicationComponentTag.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), (entity, identifier, tag, fullyQualifiedName) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(newEntity(identifier.get().child("resources"), IosResourceSetSpec.class, it -> it.ownedBy(entity))).configure(IosResourceSet.class, sourceSet -> sourceSet.from("src/" + fullyQualifiedName.get() + "/resources"));
		})));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(IosApplicationComponentTag.class), ModelComponentReference.of(LinkedVariantsComponent.class), (entity, tag, variants) -> {
			val component = ModelNodeUtils.get(entity, DefaultIosApplicationComponent.class);

			Streams.zip(component.getBuildVariants().get().stream(), Streams.stream(variants), (buildVariant, variant) -> {
				val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();

				iosApplicationVariant(variantIdentifier, component, project).getComponents().forEach(variant::addComponent);
				variant.addComponent(new BuildVariantComponent(buildVariant));
				ModelStates.register(variant);
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
		variants(project).withType(DefaultIosApplicationVariant.class).configureEach(variant -> {
			final IosApplicationOutgoingDependencies outgoing = new IosApplicationOutgoingDependencies(variant.getRuntimeElements().getAsConfiguration(), project.getObjects());
			outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
		});
	}

	private static ModelRegistration iosApplicationVariant(VariantIdentifier identifier, DefaultIosApplicationComponent component, Project project) {
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(NativeVariantTag.class)
			.mergeFrom(tagsOf(DefaultIosApplicationVariant.class))
			.withComponent(createdUsing(of(DefaultIosApplicationVariant.class), () -> {
				val variant = project.getObjects().newInstance(DefaultIosApplicationVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), (Factory<ComponentSources>) () -> project.getObjects().newInstance(ComponentSources.class), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}));
				variant.getProductBundleIdentifier().convention(component.getGroupId().map(it -> it + "." + component.getModuleName().get()));
				return variant;
			}))
			.build()
			;
	}
}
