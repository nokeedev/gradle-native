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

import com.google.common.collect.ImmutableMap;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.PolymorphicDomainObjectRegistry;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.internal.rules.RegisterAssembleLifecycleTaskRule;
import dev.nokee.runtime.nativebase.*;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.maven;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.withConventionOf;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.platform.nativebase.internal.plugins.NativeApplicationPlugin.nativeApplicationVariant;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.nativeApplicationProjection;

public final class NativeApplicationComponentModelRegistrationFactory {
	private final Class<Component> componentType;
	private final Class<? extends Component> implementationComponentType;
	private final BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration;
	private final Project project;

	public <T extends Component> NativeApplicationComponentModelRegistrationFactory(Class<? super T> componentType, Class<T> implementationComponentType, Project project, BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration) {
		this.componentType = (Class<Component>) componentType;
		this.implementationComponentType = implementationComponentType;
		this.sourceRegistration = sourceRegistration;
		this.project = project;
	}

	public ModelRegistration create(ComponentIdentifier identifier) {
		val entityPath = ModelPath.path(identifier.getName().get());
		val name = entityPath.getName();
		return ModelRegistration.builder()
			.withComponent(entityPath)
			.withComponent(createdUsing(of(componentType), () -> project.getObjects().newInstance(implementationComponentType)))
			.withComponent(identifier)
			.withComponent(IsComponent.tag())
			.withComponent(new FullyQualifiedName(ComponentNamer.INSTANCE.determineName(identifier)))
			// TODO: Should configure FileCollection on CApplication
			//   and link FileCollection to source sets
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofProjection(LanguageSourceSet.class).asDomainObject(), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, sourceSet, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(ComponentName.of(name))).accept(sourceSet);
				}
			}))
			.withComponent(createdUsing(of(DefaultNativeApplicationComponent.class), nativeApplicationProjection(name, project)))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;
				@Override
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						val registry = project.getExtensions().getByType(ModelRegistry.class);

						sourceRegistration.accept(entity, path);

						val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
						registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "dependencies"), NativeApplicationComponentDependencies.class, ModelBackedNativeApplicationComponentDependencies::new));

						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("implementation"), identifier)));
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("compileOnly"), identifier)));
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("linkOnly"), identifier)));
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("runtimeOnly"), identifier)));

						registry.register(ModelRegistration.builder()
							.withComponent(path.child("developmentVariant"))
							.withComponent(IsModelProperty.tag())
							.withComponent(createdUsing(of(new TypeOf<Property<NativeApplication>>() {}), () -> project.getObjects().property(NativeApplication.class)))
							.build());

						val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
						val buildVariants = entity.addComponent(new BuildVariants(entity, project.getProviders(), project.getObjects()));
						registry.register(dimensions.newAxisProperty(path.child("targetLinkages"))
							.elementType(TargetLinkage.class)
							.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
							.defaultValue(TargetLinkages.EXECUTABLE)
							.build());
						registry.register(dimensions.newAxisProperty(path.child("targetBuildTypes"))
							.elementType(TargetBuildType.class)
							.axis(BuildType.BUILD_TYPE_COORDINATE_AXIS)
							.defaultValue(TargetBuildTypes.DEFAULT)
							.build());
						registry.register(dimensions.newAxisProperty(path.child("targetMachines"))
							.axis(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)
							.defaultValue(TargetMachines.host())
							.build());
						registry.register(dimensions.buildVariants(path.child("buildVariants"), buildVariants.get()));

						registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "variants"), NativeApplication.class));

						registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "binaries")));

						registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "tasks")));
					}
				}
			}))
			.action(new RegisterAssembleLifecycleTaskRule(identifier, PolymorphicDomainObjectRegistry.of(project.getTasks()), project.getExtensions().getByType(ModelRegistry.class), project.getProviders()))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;
				@Override
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						ModelNodeUtils.get(entity, BaseComponent.class).getDimensions().convention(entity.getComponent(componentOf(BuildVariants.class)).dimensions());
						ModelNodeUtils.get(entity, BaseComponent.class).getBaseName().convention(path.getName());
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, path, ignored) -> {
				if (entityPath.equals(path)) {
					new CalculateNativeApplicationVariantAction(project).execute(entity, path);
				}
			}))
			.build()
			;
	}

	private static class CalculateNativeApplicationVariantAction extends ModelActionWithInputs.ModelAction1<ModelPath> {
		private final Project project;

		public CalculateNativeApplicationVariantAction(Project project) {
			this.project = project;
		}

		@Override
		protected void execute(ModelNode entity, ModelPath path) {
			val component = ModelNodeUtils.get(entity, ModelType.of(DefaultNativeApplicationComponent.class));
			component.finalizeExtension(null);
			component.getDevelopmentVariant().set(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get()))); // TODO: VariantView#get should force finalize the component.

			val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
			component.getBuildVariants().get().forEach(new Consumer<BuildVariantInternal>() {
				@Override
				public void accept(BuildVariantInternal buildVariant) {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultNativeApplicationVariant.class).build();
					val variant = ModelNodeUtils.register(entity, nativeApplicationVariant(variantIdentifier, component, project));

					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(NativeApplication.class), ModelNodes.of(variant).getComponent(componentOf(VariantComponentDependencies.class)));
				}

				private void onEachVariantDependencies(DomainObjectProvider<NativeApplication> variant, VariantComponentDependencies<?> dependencies) {
					dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
				}
			});
			entity.addComponent(new Variants(variants.build()));
		}
	}
}
