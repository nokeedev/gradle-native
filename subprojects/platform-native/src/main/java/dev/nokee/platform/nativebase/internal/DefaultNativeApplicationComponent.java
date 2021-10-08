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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.Finalizable;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantCollection;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.*;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultNativeApplicationComponent extends BaseNativeComponent<DefaultNativeApplicationVariant> implements DependencyAwareComponent<NativeApplicationComponentDependencies>, BinaryAwareComponent, Component, SourceAwareComponent<ComponentSources>, Finalizable {
	private final DefaultNativeApplicationComponentDependencies dependencies;
	private final TaskRegistry taskRegistry;
	private final NativeApplicationComponentVariants componentVariants;
	private final BinaryView<Binary> binaries;

	@Inject
	public DefaultNativeApplicationComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, ModelLookup modelLookup) {
		super(identifier, DefaultNativeApplicationVariant.class, objects, tasks, eventPublisher, taskRegistry, taskViewFactory);
		this.taskRegistry = taskRegistry;
		val dependenciesPath = ModelPath.path("components" + identifier.getPath().child("dependencies").getPath().replace(':', '.'));
		if (modelLookup.has(dependenciesPath)) {
			this.dependencies = ModelNodeUtils.get(modelLookup.get(dependenciesPath), DefaultNativeApplicationComponentDependencies.class);
		} else {
			val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(objects, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurations), dependencyHandler)));
			this.dependencies = objects.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
		}
		this.componentVariants = new NativeApplicationComponentVariants(objects, this, dependencyHandler, configurations, providers, taskRegistry, eventPublisher, viewFactory, variantRepository, binaryViewFactory, modelLookup);
		this.binaries = binaryViewFactory.create(identifier);
	}

	@Override
	public DefaultNativeApplicationComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public Provider<DefaultNativeApplicationVariant> getDevelopmentVariant() {
		return componentVariants.getDevelopmentVariant();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantCollection<DefaultNativeApplicationVariant> getVariantCollection() {
		return componentVariants.getVariantCollection();
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return componentVariants.getBuildVariants();
	}

	public void finalizeExtension(Project project) {
		getVariants().whenElementKnown(new CreateNativeBinaryLifecycleTaskRule(taskRegistry));
		getVariants().whenElementKnown(this::createBinaries);
		getVariants().whenElementKnown(new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		getVariants().whenElementKnown(new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);

		componentVariants.calculateVariants();
	}

	@Override
	public void finalizeValue() {
		finalizeExtension(null);
	}
}
