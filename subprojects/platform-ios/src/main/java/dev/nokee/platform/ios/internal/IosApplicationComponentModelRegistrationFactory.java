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
import com.google.common.collect.Iterables;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryConfigurer;
import dev.nokee.platform.base.internal.binaries.BinaryRepository;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.DevelopmentVariantConvention;
import lombok.val;
import lombok.var;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;

import java.util.Optional;
import java.util.function.BiConsumer;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.maven;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.withConventionOf;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public final class IosApplicationComponentModelRegistrationFactory {
	private final Class<Component> componentType;
	private final Class<? extends Component> implementationComponentType;
	private final BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration;
	private final Project project;

	public <T extends Component> IosApplicationComponentModelRegistrationFactory(Class<? super T> componentType, Class<T> implementationComponentType, Project project, BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration) {
		this.componentType = (Class<Component>) componentType;
		this.implementationComponentType = implementationComponentType;
		this.sourceRegistration = sourceRegistration;
		this.project = project;
	}

	public NodeRegistration create(String name) {
		return NodeRegistration.unmanaged(name, of(componentType), () -> project.getObjects().newInstance(implementationComponentType))
			.action(allDirectDescendants(mutate(of(LanguageSourceSet.class)))
				.apply(executeUsingProjection(of(LanguageSourceSet.class), withConventionOf(maven(ComponentName.of(name)))::accept)))
			.withComponent(IsComponent.tag())
			.withComponent(createdUsing(of(DefaultIosApplicationComponent.class), () -> create(name, project)))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);

				sourceRegistration.accept(entity, path);

				val componentIdentifier = ComponentIdentifier.of(ComponentName.of(name), DefaultIosApplicationComponent.class, ProjectIdentifier.of(project));
				val dependencyContainer = project.getObjects().newInstance(DefaultComponentDependencies.class, componentIdentifier, new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.getConfigurations()), project.getDependencies())));
				val dependencies = project.getObjects().newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);

				registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(path.child("dependencies"), dependencies));

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("implementation"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getImplementation().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getImplementation()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getImplementation().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("compileOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getCompileOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getCompileOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getCompileOnly().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getLinkOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getLinkOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getLinkOnly().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getRuntimeOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getRuntimeOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getRuntimeOnly().getAsConfiguration().getName())))
					.build());

				registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(path.child("binaries")));

				registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(path.child("variants"), DefaultIosApplicationVariant.class));
			})))
			.action(self(stateOf(ModelState.Realized)).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				ModelNodeUtils.get(entity, BaseComponent.class).getBaseName().convention(path.getName());
			})))
			.action(self(stateOf(ModelState.Finalized)).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val component = ModelNodeUtils.get(entity, DefaultIosApplicationComponent.class);
				component.finalizeValue();

				val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
				component.getBuildVariants().get().forEach(buildVariant -> {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultIosApplicationVariant.class).build();

					val variant = ModelNodeUtils.register(entity, iosApplicationVariant(variantIdentifier, component, project));
					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(DefaultIosApplicationVariant.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));
				});
				entity.addComponent(new Variants(variants.build()));
			})))
			;
	}

	private static void onEachVariantDependencies(DomainObjectProvider<DefaultIosApplicationVariant> variant, VariantComponentDependencies<?> dependencies) {
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}

	private static NodeRegistration iosApplicationVariant(VariantIdentifier<DefaultIosApplicationVariant> identifier, DefaultIosApplicationComponent component, Project project) {
		val taskRegistry = project.getExtensions().getByType(TaskRegistry.class);
		val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier));
		val variantDependencies = newDependencies((BuildVariantInternal) identifier.getBuildVariant(), identifier, component, project.getObjects(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(ModelLookup.class));
		return NodeRegistration.unmanaged(identifier.getUnambiguousName(), of(DefaultIosApplicationVariant.class), () -> {
			val variant = project.getObjects().newInstance(DefaultIosApplicationVariant.class, identifier, project.getObjects(), project.getProviders(), assembleTask, project.getExtensions().getByType(BinaryViewFactory.class));
			variant.getProductBundleIdentifier().convention(component.getGroupId().map(it -> it + "." + component.getModuleName().get()));
			return variant;
		})
			.withComponent(identifier)
			.withComponent(IsVariant.tag())
			.withComponent(variantDependencies)
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val dependencies = variantDependencies.getDependencies();

				registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(path.child("dependencies"), dependencies));

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("implementation"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getImplementation().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getImplementation()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getImplementation().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("compileOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getCompileOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getCompileOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getCompileOnly().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getLinkOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getLinkOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getLinkOnly().getAsConfiguration().getName())))
					.build());
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getRuntimeOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getRuntimeOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getRuntimeOnly().getAsConfiguration().getName())))
					.build());

				registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(path.child("binaries")));

				val executableIdentifier = BinaryIdentifier.of(BinaryName.of("executable"), ExecutableBinaryInternal.class, identifier);
				val executable = registry.register(ModelRegistration.builder()
					.withComponent(path.child("executable"))
					.withComponent(IsBinary.tag())
					.withComponent(executableIdentifier)
					.withComponent(createdUsing(of(ExecutableBinaryInternal.class), () -> {
						return project.getExtensions().getByType(BinaryRepository.class).get(executableIdentifier);
					}))
					.build());
				project.getExtensions().getByType(BinaryConfigurer.class).configure(executableIdentifier, binary -> ModelStates.realize(ModelNodes.of(executable)));

				val applicationBundleIdentifier = BinaryIdentifier.of(BinaryName.of("applicationBundle"), IosApplicationBundleInternal.class, identifier);
				val applicationBundle = registry.register(ModelRegistration.builder()
					.withComponent(path.child("applicationBundle"))
					.withComponent(IsBinary.tag())
					.withComponent(applicationBundleIdentifier)
					.withComponent(createdUsing(of(IosApplicationBundleInternal.class), () -> {
						return project.getExtensions().getByType(BinaryRepository.class).get(applicationBundleIdentifier);
					}))
					.build());
				project.getExtensions().getByType(BinaryConfigurer.class).configure(applicationBundleIdentifier, binary -> ModelStates.realize(ModelNodes.of(applicationBundle)));

				val signedApplicationBundleIdentifier = BinaryIdentifier.of(BinaryName.of("signedApplicationBundle"), SignedIosApplicationBundleInternal.class, identifier);
				val signedApplicationBundle = registry.register(ModelRegistration.builder()
					.withComponent(path.child("signedApplicationBundle"))
					.withComponent(IsBinary.tag())
					.withComponent(signedApplicationBundleIdentifier)
					.withComponent(createdUsing(of(SignedIosApplicationBundleInternal.class), () -> {
						return project.getExtensions().getByType(BinaryRepository.class).get(signedApplicationBundleIdentifier);
					}))
					.build());
				project.getExtensions().getByType(BinaryConfigurer.class).configure(signedApplicationBundleIdentifier, binary -> ModelStates.realize(ModelNodes.of(signedApplicationBundle)));

				whenElementKnown(entity, ModelActionWithInputs.of(ModelComponentReference.ofAny(projectionOf(Configuration.class)), ModelComponentReference.of(ModelPath.class), (e, ignored, p) -> {
					((NamedDomainObjectProvider<Configuration>) ModelNodeUtils.get(e, NamedDomainObjectProvider.class)).configure(configuration -> {
						val parentConfigurationResult = project.getExtensions().getByType(ModelLookup.class).query(ModelSpecs.of(ModelNodes.withPath(path.getParent().get().child(p.getName()))));
						Optional.ofNullable(Iterables.getOnlyElement(parentConfigurationResult.get(), null)).ifPresent(parentConfigurationEntity -> {
							val parentConfiguration = ModelNodeUtils.get(parentConfigurationEntity, Configuration.class);
							if (!parentConfiguration.getName().equals(configuration.getName())) {
								configuration.extendsFrom(parentConfiguration);
							}
						});
					});
				}));
			})))
			;
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}

	private static VariantComponentDependencies<DefaultNativeComponentDependencies> newDependencies(BuildVariantInternal buildVariant, VariantIdentifier<DefaultIosApplicationVariant> variantIdentifier, DefaultIosApplicationComponent component, ObjectFactory objectFactory, ConfigurationContainer configurationContainer, DependencyHandler dependencyHandler, ModelLookup modelLookup) {
		var variantDependencies = component.getDependencies();
		if (component.getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant).withOwnerIdentifier(variantIdentifier).withBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));

		boolean hasSwift = modelLookup.anyMatch(ModelSpecs.of(ModelNodes.withType(of(SwiftSourceSet.class))));
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(IosApplicationOutgoingDependencies.class, variantIdentifier, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	private static DefaultIosApplicationComponent create(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultIosApplicationComponent.class, ProjectIdentifier.of(project));
		val result = new DefaultIosApplicationComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class));
		result.getDevelopmentVariant().convention(project.getProviders().provider(new DevelopmentVariantConvention<>(() -> result.getVariants().get())));
		return result;
	}
}
