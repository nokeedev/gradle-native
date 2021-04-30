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
package dev.nokee.platform.ios.internal;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.DevelopmentVariantConvention;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskProvider;

import java.util.ArrayList;

import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public final class IosComponentVariants implements ComponentVariants {
	@Getter private final VariantCollection<DefaultIosApplicationVariant> variantCollection;
	@Getter private final SetProperty<BuildVariantInternal> buildVariants;
	@Getter private final Provider<DefaultIosApplicationVariant> developmentVariant;
	private final ObjectFactory objectFactory;
	private final DefaultIosApplicationComponent component;
	private final DependencyHandler dependencyHandler;
	private final ConfigurationContainer configurationContainer;
	private final ProviderFactory providerFactory;
	private final TaskRegistry taskRegistry;
	private final BinaryViewFactory binaryViewFactory;
	private final FunctionalSourceSet sourceView;

	public IosComponentVariants(ObjectFactory objectFactory, DefaultIosApplicationComponent component, DependencyHandler dependencyHandler, ConfigurationContainer configurationContainer, ProviderFactory providerFactory, TaskRegistry taskRegistry, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, FunctionalSourceSet sourceView) {
		this.binaryViewFactory = binaryViewFactory;
		this.sourceView = sourceView;
		this.variantCollection = new VariantCollection<>(component.getIdentifier(), DefaultIosApplicationVariant.class, eventPublisher, viewFactory, variantRepository);
		this.buildVariants = objectFactory.setProperty(BuildVariantInternal.class);
		this.developmentVariant = providerFactory.provider(new DevelopmentVariantConvention<>(() -> getVariantCollection().get()));
		this.objectFactory = objectFactory;
		this.component = component;
		this.dependencyHandler = dependencyHandler;
		this.configurationContainer = configurationContainer;
		this.providerFactory = providerFactory;
		this.taskRegistry = taskRegistry;
	}

	public void calculateVariants() {
		getBuildVariants().get().forEach(buildVariant -> {
			val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultIosApplicationVariant.class).build();

			val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), variantIdentifier));

			val dependencies = newDependencies(buildVariant, variantIdentifier);
			val variant = getVariantCollection().registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies, assembleTask));

			onEachVariantDependencies(variant, dependencies);
		});
	}

	private DefaultIosApplicationVariant createVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<DefaultNativeComponentDependencies> variantDependencies, TaskProvider<Task> assembleTask) {
		val variant = objectFactory.newInstance(DefaultIosApplicationVariant.class, identifier, variantDependencies, objectFactory, providerFactory, assembleTask, binaryViewFactory);
		variant.getProductBundleIdentifier().convention(component.getGroupId().map(it -> it + "." + component.getModuleName().get()));
		return variant;
	}

	private VariantComponentDependencies<DefaultNativeComponentDependencies> newDependencies(BuildVariantInternal buildVariant, VariantIdentifier<DefaultIosApplicationVariant> variantIdentifier) {
		var variantDependencies = component.getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultNativeComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				component.getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant);

		// FIXME!!!!
		val swiftSources = new ArrayList<KnownDomainObject<SwiftSourceSet>>();
		sourceView.whenElementKnownEx(SwiftSourceSet.class, swiftSources::add);
		boolean hasSwift = !swiftSources.isEmpty();
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(IosApplicationOutgoingDependencies.class, variantIdentifier, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	private void onEachVariantDependencies(VariantProvider<DefaultIosApplicationVariant> variant, VariantComponentDependencies<?> dependencies) {
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}
}
