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
package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.BaseDomainObjectViewProjection;
import dev.nokee.model.internal.BaseNamedDomainObjectViewProjection;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryRepository;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationExtension;
import dev.nokee.platform.nativebase.NativeApplicationSources;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Optional;

import static dev.nokee.model.internal.core.ModelNodes.discover;
import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.core.ModelProjections.*;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class NativeApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public NativeApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		val components = project.getExtensions().getByType(ComponentContainer.class);
		ModelNodeUtils.get(ModelNodes.of(components), NodeRegistrationFactoryRegistry.class).registerFactory(of(NativeApplicationExtension.class), name -> nativeApplication(name, project));
		val componentProvider = components.register("main", NativeApplicationExtension.class, configureUsingProjection(DefaultNativeApplicationComponent.class, baseNameConvention(project.getName()).andThen(configureBuildVariants())));
		val extension = componentProvider.get();

		// Other configurations
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));
		project.afterEvaluate(getObjects().newInstance(TargetBuildTypeRule.class, extension.getTargetBuildTypes(), EXTENSION_NAME));
		project.afterEvaluate(finalizeModelNodeOf(componentProvider));

		project.getExtensions().add(NativeApplicationExtension.class, EXTENSION_NAME, extension);
	}

	public static NodeRegistration nativeApplication(String name, Project project) {
		return new NativeApplicationComponentModelRegistrationFactory(NativeApplicationExtension.class, project, (entity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

			// TODO: Should be created using CHeaderSetSpec
			val headers = registry.register(ModelRegistration.builder()
				.withComponent(path.child("headers"))
				.withComponent(managed(of(CHeaderSet.class)))
				.withComponent(managed(of(BaseLanguageSourceSetProjection.class)))
				.build());

			// TODO: Should be created as ModelProperty (readonly) with CApplicationSources projection
			registry.register(ModelRegistration.builder()
				.withComponent(path.child("sources"))
				.withComponent(IsModelProperty.tag())
				.withComponent(managed(of(NativeApplicationSources.class)))
				.withComponent(managed(of(BaseDomainObjectViewProjection.class)))
				.withComponent(managed(of(BaseNamedDomainObjectViewProjection.class)))
				.build());

			registry.register(propertyFactory.create(path.child("sources").child("headers"), ModelNodes.of(headers)));
		}).create(name);
	}


	public static NodeRegistration nativeApplicationVariant(VariantIdentifier<DefaultNativeApplicationVariant> identifier, DefaultNativeApplicationComponent component, Project project) {
		val variantDependencies = newDependencies((BuildVariantInternal) identifier.getBuildVariant(), identifier, component, project.getConfigurations(), project.getDependencies(), project.getObjects(), project.getExtensions().getByType(ModelLookup.class));
		return NodeRegistration.unmanaged(identifier.getUnambiguousName(), of(NativeApplication.class), () -> {
				val taskRegistry = project.getExtensions().getByType(TaskRegistry.class);
				val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier));

				return project.getObjects().newInstance(DefaultNativeApplicationVariant.class, identifier, variantDependencies.getIncoming(), project.getObjects(), project.getProviders(), assembleTask, project.getExtensions().getByType(BinaryViewFactory.class));
			})
			.withComponent(IsVariant.tag())
			.withComponent(identifier)
			.withComponent(variantDependencies)
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("executable"), ExecutableBinaryInternal.class, identifier); // TODO: Use input to get variant identifier
				val executable = registry.register(ModelRegistration.builder()
					.withComponent(path.child("executable"))
					.withComponent(binaryIdentifier)
					.withComponent(createdUsing(of(ExecutableBinary.class), () -> {
						ModelStates.realize(entity);
						return project.getExtensions().getByType(BinaryRepository.class).get(binaryIdentifier);
					}))
					.build());

				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);
				project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (e, p) -> {
					if (path.getParent().get().child("binaries").equals(p)) {
						registry.register(propertyFactory.create(p.child(Optional.of(identifier.getUnambiguousName()).filter(it -> !it.isEmpty()).map(it -> it + "Executable").orElse("executable")), ModelNodes.of(executable)));
					}
				}));

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("binaries"))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(of(BinaryView.class), () -> new BinaryViewAdapter<>(new ViewAdapter<>(Binary.class, new ModelNodeBackedViewStrategy(project.getProviders())))))
					.build());

				registry.register(propertyFactory.create(path.child("binaries").child("executable"), ModelNodes.of(executable)));

				registry.register(ModelRegistration.builder()
					.withComponent(path.child("dependencies"))
					.withComponent(IsModelProperty.tag())
					.withComponent(ofInstance(variantDependencies.getDependencies()))
					.build());

				val implementation = registry.register(ModelRegistration.builder()
					.withComponent(path.child("implementation"))
					.withComponent(createdUsing(of(Configuration.class), () -> variantDependencies.getDependencies().getImplementation().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> variantDependencies.getDependencies().getImplementation()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(variantDependencies.getDependencies().getImplementation().getAsConfiguration().getName())))
					.build());
				val compileOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("compileOnly"))
					.withComponent(createdUsing(of(Configuration.class), () -> variantDependencies.getDependencies().getCompileOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> variantDependencies.getDependencies().getCompileOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(variantDependencies.getDependencies().getCompileOnly().getAsConfiguration().getName())))
					.build());
				val linkOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkOnly"))
					.withComponent(createdUsing(of(Configuration.class), () -> variantDependencies.getDependencies().getLinkOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> variantDependencies.getDependencies().getLinkOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(variantDependencies.getDependencies().getLinkOnly().getAsConfiguration().getName())))
					.build());
				val runtimeOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeOnly"))
					.withComponent(createdUsing(of(Configuration.class), () -> variantDependencies.getDependencies().getRuntimeOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> variantDependencies.getDependencies().getRuntimeOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(variantDependencies.getDependencies().getRuntimeOnly().getAsConfiguration().getName())))
					.build());
				registry.register(propertyFactory.create(path.child("dependencies").child("implementation"), ModelNodes.of(implementation)));
				registry.register(propertyFactory.create(path.child("dependencies").child("compileOnly"), ModelNodes.of(compileOnly)));
				registry.register(propertyFactory.create(path.child("dependencies").child("linkOnly"), ModelNodes.of(linkOnly)));
				registry.register(propertyFactory.create(path.child("dependencies").child("runtimeOnly"), ModelNodes.of(runtimeOnly)));
			})))
			;
	}

	private static VariantComponentDependencies<DefaultNativeApplicationComponentDependencies> newDependencies(BuildVariantInternal buildVariant, VariantIdentifier<DefaultNativeApplicationVariant> variantIdentifier, DefaultNativeApplicationComponent component, ConfigurationContainer configurationContainer, DependencyHandler dependencyHandler, ObjectFactory objectFactory, ModelLookup modelLookup) {
		var variantDependencies = component.getDependencies();
		if (component.getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				component.getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		boolean hasSwift = modelLookup.anyMatch(ModelSpecs.of(withType(of(SwiftSourceSet.class))));
		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant).withOwnerIdentifier(variantIdentifier).withBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		val incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(NativeApplicationOutgoingDependencies.class, variantIdentifier, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}
}
