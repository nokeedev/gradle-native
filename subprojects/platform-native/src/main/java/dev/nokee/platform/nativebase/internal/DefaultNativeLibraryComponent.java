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

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.CreateNativeBinaryLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantObjectsLifecycleTaskRule;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.Set;

import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.core.ModelNodeUtils.instantiate;
import static dev.nokee.model.internal.type.GradlePropertyTypes.property;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;

public class DefaultNativeLibraryComponent extends BaseNativeComponent<DefaultNativeLibraryVariant> implements Component
	, DependencyAwareComponent<NativeLibraryComponentDependencies>
	, ModelBackedSourceAwareComponentMixIn<ComponentSources, NativeLibrarySourcesAdapter>
	, ModelBackedVariantAwareComponentMixIn<DefaultNativeLibraryVariant>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedHasDevelopmentVariantMixIn<DefaultNativeLibraryVariant>
	, ModelBackedNamedMixIn
	, ModelBackedTargetMachineAwareComponentMixIn
	, ModelBackedTargetBuildTypeAwareComponentMixIn
	, ModelBackedTargetLinkageAwareComponentMixIn
{
	private final TaskRegistry taskRegistry;

	@Inject
	public DefaultNativeLibraryComponent(ComponentIdentifier identifier, ObjectFactory objects, TaskRegistry taskRegistry, ModelRegistry registry) {
		super(identifier, DefaultNativeLibraryVariant.class, objects, taskRegistry, registry);
		this.taskRegistry = taskRegistry;
	}

	@Override
	public NativeLibraryComponentDependencies getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as(NativeLibraryComponentDependencies.class).get();
	}

	@Override
	public void dependencies(Action<? super NativeLibraryComponentDependencies> action) {
		action.execute(getDependencies());
	}

	@Override
	public void dependencies(@SuppressWarnings("rawtypes") Closure closure) {
		dependencies(ConfigureUtil.configureUsing(closure));
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return ModelProperties.getProperty(this, "buildVariants").as(set(of(BuildVariant.class))).asProvider();
	}

	@Override
	public Property<DefaultNativeLibraryVariant> getDevelopmentVariant() {
		return ModelProperties.getProperty(this, "developmentVariant").asProperty(property(of(DefaultNativeLibraryVariant.class)));
	}

	@Override
	@SuppressWarnings("unchecked")
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public VariantView<DefaultNativeLibraryVariant> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	public void finalizeExtension(Project project) {
		whenElementKnown(this, new CreateNativeBinaryLifecycleTaskRule(taskRegistry));
		whenElementKnown(this, this::createBinaries);
		whenElementKnown(this, new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		whenElementKnown(this, new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
	}

	private static void whenElementKnown(Object target, Action<? super KnownDomainObject<DefaultNativeLibraryVariant>> action) {
		instantiate(ModelNodes.of(target), ModelAction.whenElementKnown(ownedBy(ModelNodes.of(target).getId()), DefaultNativeLibraryVariant.class, action));
	}
}
