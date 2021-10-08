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

import com.google.common.base.Suppliers;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.Finalizable;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.type.ModelType;
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
import dev.nokee.platform.base.internal.variants.VariantViewInternal;
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
import java.util.function.Supplier;

public class DefaultNativeApplicationComponent extends BaseNativeComponent<DefaultNativeApplicationVariant> implements DependencyAwareComponent<NativeApplicationComponentDependencies>, BinaryAwareComponent, Component, SourceAwareComponent<ComponentSources>, Finalizable {
	private final DefaultNativeApplicationComponentDependencies dependencies;
	private final TaskRegistry taskRegistry;
	private final Supplier<NativeApplicationComponentVariants> componentVariants;
	private final BinaryView<Binary> binaries;
	private final Supplier<VariantViewInternal<DefaultNativeApplicationVariant>> variants;
	private final boolean isFinalizable;

	@Inject
	public DefaultNativeApplicationComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ConfigurationContainer configurations, DependencyHandler dependencyHandler, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, ModelLookup modelLookup) {
		super(identifier, DefaultNativeApplicationVariant.class, objects, tasks, eventPublisher, taskRegistry, taskViewFactory);
		this.taskRegistry = taskRegistry;
		val dependenciesPath = ModelPath.path("components" + identifier.getPath().child("dependencies").getPath().replace(':', '.'));
		if (modelLookup.has(dependenciesPath)) {
			this.isFinalizable = false;
			this.dependencies = ModelNodeUtils.get(modelLookup.get(dependenciesPath), DefaultNativeApplicationComponentDependencies.class);
		} else {
			this.isFinalizable = true;
			val dependencyContainer = objects.newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(objects, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurations), dependencyHandler)));
			this.dependencies = objects.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
		}
		val variantsPath = ModelPath.path("components" + identifier.getPath().child("variants").getPath().replace(':', '.'));
		if (modelLookup.has(variantsPath)) {
			this.componentVariants = () -> ModelNodeUtils.get(ModelNodes.of(this), ModelType.of(NativeApplicationComponentVariants.class));
			this.variants = () -> (VariantViewInternal<DefaultNativeApplicationVariant>) ModelNodeUtils.get(modelLookup.get(variantsPath), VariantView.class);
		} else {
			this.componentVariants = Suppliers.ofInstance(new NativeApplicationComponentVariants(objects, this, dependencyHandler, configurations, providers, taskRegistry, eventPublisher, viewFactory, variantRepository, binaryViewFactory, modelLookup));
			this.variants = Suppliers.ofInstance(componentVariants.get().getVariantCollection().getAsView(DefaultNativeApplicationVariant.class));
		}

		val binariesPath = ModelPath.path("components" + identifier.getPath().child("binaries").getPath().replace(':', '.'));
		if (modelLookup.has(binariesPath)) {
			this.binaries = (BinaryView<Binary>) ModelNodeUtils.get(modelLookup.get(binariesPath), BinaryView.class);
		} else {
			this.binaries = binaryViewFactory.create(identifier);
		}
	}

	@Override
	public DefaultNativeApplicationComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public Provider<DefaultNativeApplicationVariant> getDevelopmentVariant() {
		return getComponentVariants().getDevelopmentVariant();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return binaries;
	}

	@Override
	public VariantCollection<DefaultNativeApplicationVariant> getVariantCollection() {
		return getComponentVariants().getVariantCollection();
	}

	public VariantViewInternal<DefaultNativeApplicationVariant> getVariants() {
		return variants.get();
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return getComponentVariants().getBuildVariants();
	}

	private NativeApplicationComponentVariants getComponentVariants() {
		return componentVariants.get();
	}

	public void finalizeExtension(Project project) {
		getVariants().whenElementKnown(new CreateNativeBinaryLifecycleTaskRule(taskRegistry));
		getVariants().whenElementKnown(this::createBinaries);
		getVariants().whenElementKnown(new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		getVariants().whenElementKnown(new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);

		getComponentVariants().calculateVariants();
	}

	@Override
	public void finalizeValue() {
		if (isFinalizable) {
			finalizeExtension(null);
		}
	}
}
