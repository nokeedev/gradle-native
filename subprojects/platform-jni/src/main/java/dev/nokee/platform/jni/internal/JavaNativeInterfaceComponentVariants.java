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
package dev.nokee.platform.jni.internal;

import com.google.common.base.Preconditions;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.ComponentVariants;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import lombok.Getter;
import lombok.val;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

public final class JavaNativeInterfaceComponentVariants implements ComponentVariants {
	@Getter private final VariantCollection<JniLibraryInternal> variantCollection;
	private final ObjectFactory objectFactory;
	private final JniLibraryComponentInternal component;
	private final ConfigurationContainer configurationContainer;
	private final DependencyHandler dependencyHandler;
	private final ProviderFactory providerFactory;
	private final TaskRegistry taskRegistry;
	private final DomainObjectEventPublisher eventPublisher;
	private final BinaryViewFactory binaryViewFactory;
	private final TaskViewFactory taskViewFactory;
	private final ModelLookup modelLookup;

	public JavaNativeInterfaceComponentVariants(ObjectFactory objectFactory, JniLibraryComponentInternal component, ConfigurationContainer configurationContainer, DependencyHandler dependencyHandler, ProviderFactory providerFactory, TaskRegistry taskRegistry, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskViewFactory taskViewFactory, ModelLookup modelLookup) {
		this.eventPublisher = eventPublisher;
		this.binaryViewFactory = binaryViewFactory;
		this.taskViewFactory = taskViewFactory;
		this.modelLookup = modelLookup;
		this.variantCollection = new VariantCollection<>(component.getIdentifier(), JniLibraryInternal.class, eventPublisher, viewFactory, variantRepository);
		this.objectFactory = objectFactory;
		this.component = component;
		this.configurationContainer = configurationContainer;
		this.dependencyHandler = dependencyHandler;
		this.providerFactory = providerFactory;
		this.taskRegistry = taskRegistry;
	}

	public void calculateVariants() {
		component.getBuildVariants().get().forEach(buildVariant -> {
			val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(JniLibraryInternal.class).build();

			val dependencies = newDependencies(buildVariant, component, variantIdentifier);
			variantCollection.registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies));
		});
	}

	private JniLibraryInternal createVariant(VariantIdentifier<JniLibraryInternal> identifier, VariantComponentDependencies variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		Preconditions.checkArgument(buildVariant.hasAxisValue(TARGET_MACHINE_COORDINATE_AXIS));

		val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(LifecycleBasePlugin.ASSEMBLE_TASK_NAME), identifier), task -> {
			task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
			task.setDescription(String.format("Assembles the '%s' outputs of this project.", BuildVariantNamer.INSTANCE.determineName((BuildVariantInternal)identifier.getBuildVariant())));
		});

		val result = objectFactory.newInstance(JniLibraryInternal.class, identifier, component.getSources(), component.getGroupId(), variantDependencies, objectFactory, configurationContainer, providerFactory, taskRegistry, assembleTask, eventPublisher, binaryViewFactory, taskViewFactory);
		return result;
	}

	private VariantComponentDependencies newDependencies(BuildVariantInternal buildVariant, JniLibraryComponentInternal component, VariantIdentifier<JniLibraryInternal> variantIdentifier) {
		DefaultJavaNativeInterfaceNativeComponentDependencies variantDependencies = component.getDependencies();
		if (component.getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultJavaNativeInterfaceNativeComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				component.getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(new NativeComponentDependenciesJavaNativeInterfaceAdapter(variantDependencies)).withVariant(buildVariant).withOwnerIdentifier(variantIdentifier).withBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
		boolean hasSwift = modelLookup.anyMatch(ModelSpecs.of(withType(of(SwiftSourceSet.class))));
		boolean hasHeader = modelLookup.anyMatch(ModelSpecs.of(withType(of(CSourceSet.class)).or(withType(of(CppSourceSet.class))).or(withType(of(ObjectiveCSourceSet.class))).or(withType(of(ObjectiveCppSourceSet.class)))));
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else if (hasHeader) {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(objectFactory);

		return new VariantComponentDependencies(variantDependencies, incoming);
	}
}
