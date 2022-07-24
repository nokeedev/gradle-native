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
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedHasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.developmentvariant.HasDevelopmentVariantMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantObjectsLifecycleTaskRule;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.Set;

import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.core.ModelNodeUtils.instantiate;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;

public class DefaultNativeApplicationComponent extends BaseNativeComponent<NativeApplication> implements Component
	, DependencyAwareComponent<NativeApplicationComponentDependencies>
	, ModelBackedSourceAwareComponentMixIn<ComponentSources, NativeApplicationSourcesAdapter>
	, ModelBackedVariantAwareComponentMixIn<NativeApplication>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, HasDevelopmentVariantMixIn<NativeApplication>
	, ModelBackedNamedMixIn
	, ModelBackedTargetMachineAwareComponentMixIn
	, ModelBackedTargetBuildTypeAwareComponentMixIn
	, ModelBackedTargetLinkageAwareComponentMixIn
	, ModelBackedHasAssembleTaskMixIn
{
	private final TaskRegistry taskRegistry;

	@Inject
	public DefaultNativeApplicationComponent(ComponentIdentifier identifier, TaskRegistry taskRegistry) {
		super(identifier);
		this.taskRegistry = taskRegistry;
	}

	@Override
	public NativeApplicationComponentDependencies getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as(NativeApplicationComponentDependencies.class).get();
	}

	@Override
	public void dependencies(Action<? super NativeApplicationComponentDependencies> action) {
		action.execute(getDependencies());
	}

	@Override
	public void dependencies(@SuppressWarnings("rawtypes") Closure closure) {
		dependencies(ConfigureUtil.configureUsing(closure));
	}

	@Override
	public Property<NativeApplication> getDevelopmentVariant() {
		return HasDevelopmentVariantMixIn.super.getDevelopmentVariant();
	}

	@Override
	@SuppressWarnings("unchecked")
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public VariantView<NativeApplication> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return ModelProperties.getProperty(this, "buildVariants").as(set(of(BuildVariant.class))).asProvider();
	}

	public void finalizeExtension(Project project) {
		whenElementKnown(this, new CreateVariantObjectsLifecycleTaskRule(taskRegistry));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		whenElementKnown(this, new CreateVariantAssembleLifecycleTaskRule(taskRegistry));
	}

	private static void whenElementKnown(Object target, Action<? super KnownDomainObject<NativeApplication>> action) {
		instantiate(ModelNodes.of(target), ModelAction.whenElementKnown(ownedBy(ModelNodes.of(target).getId()), NativeApplication.class, action));
	}
}
