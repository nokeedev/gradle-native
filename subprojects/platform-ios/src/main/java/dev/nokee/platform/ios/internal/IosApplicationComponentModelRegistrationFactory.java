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
package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableMap;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.IsVariant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.Variants;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.ExtendsFromParentConfigurationAction;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencybuckets.CompileOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.ModelBackedTaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.NativeVariantTag;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependenciesComponent;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.DevelopmentVariantConvention;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.provider.Provider;
import org.gradle.internal.Cast;

import java.util.function.BiConsumer;

import static dev.nokee.model.internal.actions.ModelAction.configureMatching;
import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.consumable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.resolvable;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public final class IosApplicationComponentModelRegistrationFactory {
	private final Class<Component> componentType;
	private final Class<Component> implementationComponentType;
	private final BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration;
	private final Project project;

	@SuppressWarnings("unchecked")
	public <T extends Component> IosApplicationComponentModelRegistrationFactory(Class<? super T> componentType, Class<T> implementationComponentType, Project project, BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration) {
		this.componentType = (Class<Component>) componentType;
		this.implementationComponentType = (Class<Component>) implementationComponentType;
		this.sourceRegistration = sourceRegistration;
		this.project = project;
	}

	public ModelRegistration create(ComponentIdentifier identifier) {
		val entityPath = ModelPath.path(identifier.getName().get());
		val builder = ModelRegistration.builder()
			.withComponent(new ModelPathComponent(entityPath))
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(createdUsing(of(implementationComponentType), () -> project.getObjects().newInstance(implementationComponentType)))
			.withComponent(IsComponent.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(createdUsing(of(DefaultIosApplicationComponent.class), () -> create(identifier.getName().get(), project)))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPathComponent, ModelState>() {
				private boolean alreadyExecuted = false;

				@Override
				public void execute(ModelNode entity, ModelPathComponent path, ModelState state) {
					if (entityPath.equals(path.get()) && state.isAtLeast(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						val registry = project.getExtensions().getByType(ModelRegistry.class);

						sourceRegistration.accept(entity, path.get());

						val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));

						val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("implementation"), identifier)));
						val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("compileOnly"), identifier)));
						val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("linkOnly"), identifier)));
						val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("runtimeOnly"), identifier)));

						entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
						entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
						entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
						entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

						val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetLinkages"))
							.elementType(TargetLinkage.class)
							.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
							.defaultValue(TargetLinkages.EXECUTABLE)
							.build());
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetBuildTypes"))
							.elementType(TargetBuildType.class)
							.axis(BuildType.BUILD_TYPE_COORDINATE_AXIS)
							.defaultValue(TargetBuildTypes.named("Default"))
							.build());
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetMachines"))
							.axis(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)
							.defaultValue(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64())
							.build());
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, path, ignored) -> {
				if (entityPath.equals(path.get())) {
					val registry = project.getExtensions().getByType(ModelRegistry.class);
					val component = ModelNodeUtils.get(entity, DefaultIosApplicationComponent.class);

					val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
					component.getBuildVariants().get().forEach(buildVariant -> {
						val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();

						val variant = registry.register(iosApplicationVariant(variantIdentifier, component, project));
						variants.put(buildVariant, ModelNodes.of(variant));
						onEachVariantDependencies(variant.as(DefaultIosApplicationVariant.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));
					});
					entity.addComponent(new Variants(variants.build()));

					component.finalizeValue();
				}
			}));

		if (identifier.isMainComponent()) {
			builder.withComponent(ExcludeFromQualifyingNameTag.tag());
		}

		return builder.build();
	}

	private static void onEachVariantDependencies(DomainObjectProvider<DefaultIosApplicationVariant> variant, VariantComponentDependencies<?> dependencies) {
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}

	private static ModelRegistration iosApplicationVariant(VariantIdentifier identifier, DefaultIosApplicationComponent component, Project project) {
		val taskRegistry = ModelBackedTaskRegistry.newInstance(project);
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(IsVariant.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(NativeVariantTag.tag())
			.withComponent(createdUsing(of(DefaultIosApplicationVariant.class), () -> {
				val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier));
				val variant = project.getObjects().newInstance(DefaultIosApplicationVariant.class, identifier, project.getObjects(), project.getProviders(), assembleTask);
				variant.getProductBundleIdentifier().convention(component.getGroupId().map(it -> it + "." + component.getModuleName().get()));
				return variant;
			}))
			.action(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ModelPathComponent.class), (entity, id, path) -> {
				if (id.get().equals(identifier)) {
					val registry = project.getExtensions().getByType(ModelRegistry.class);

					val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project)));
					val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("implementation"), identifier)));
					val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("compileOnly"), identifier)));
					val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("linkOnly"), identifier)));
					val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("runtimeOnly"), identifier)));

					entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
					entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
					entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
					entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

					val resolvableFactory = project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class);
					boolean hasSwift = project.getExtensions().getByType(ModelLookup.class).anyMatch(ModelSpecs.of(ModelNodes.withType(of(SwiftSourceSet.class))));
					if (hasSwift) {
						val importModules = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("importSwiftModules"), identifier)));
						importModules.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))
							.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.SWIFT_API))))
							.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
							.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
					} else {
						val headerSearchPaths = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("headerSearchPaths"), identifier)));
						headerSearchPaths.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))
							.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))))
							.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
							.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
					}
					val linkLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("linkLibraries"), identifier)));
					linkLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class))
						.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))))
						.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
						.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
					val runtimeLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("runtimeLibraries"), identifier)));
					runtimeLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
						.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
						.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
						.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));

					val consumableFactory = project.getExtensions().getByType(ConsumableDependencyBucketRegistrationFactory.class);
					val runtimeElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("runtimeElements"), identifier)));
					runtimeElements.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
						.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
						.andThen(ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects())));
					val outgoing = entity.addComponent(new NativeOutgoingDependenciesComponent(new IosApplicationOutgoingDependencies(ModelNodeUtils.get(ModelNodes.of(runtimeElements), Configuration.class), project.getObjects())));
					val incoming = entity.get(ModelBackedNativeIncomingDependencies.class);
					entity.addComponent(new VariantComponentDependencies<NativeComponentDependencies>(ModelProperties.getProperty(entity, "dependencies").as(NativeComponentDependencies.class)::get, incoming, outgoing.get()));

					registry.instantiate(configureMatching(ownedBy(entity.getId()).and(subtypeOf(of(Configuration.class))), new ExtendsFromParentConfigurationAction()));
				}
			})))
			.build()
			;
	}

	@SuppressWarnings("unchecked")
	private static DefaultIosApplicationComponent create(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project));
		val result = new DefaultIosApplicationComponent(Cast.uncheckedCast(identifier), project.getObjects(), project.getProviders(), project.getLayout(), project.getConfigurations(), project.getDependencies(), ModelBackedTaskRegistry.newInstance(project), project.getExtensions().getByType(ModelRegistry.class));
		result.getDevelopmentVariant().convention((Provider<? extends DefaultIosApplicationVariant>) project.getProviders().provider(new DevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) result.getVariants().map(VariantInternal.class::cast).get())));
		return result;
	}
}
