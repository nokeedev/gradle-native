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
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.CreateNativeBinaryLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAwareComponentObjectsLifecycleTaskRule;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantObjectsLifecycleTaskRule;
import dev.nokee.utils.Cast;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.Set;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.GradlePropertyTypes.property;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;

public class DefaultNativeApplicationComponent extends BaseNativeComponent<DefaultNativeApplicationVariant> implements Component
	, DependencyAwareComponent<NativeApplicationComponentDependencies>
	, ModelBackedSourceAwareComponentMixIn<ComponentSources>
	, ModelBackedVariantAwareComponentMixIn<DefaultNativeApplicationVariant>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedHasDevelopmentVariantMixIn<DefaultNativeApplicationVariant>
	, ModelBackedNamedMixIn
{
	private final TaskRegistry taskRegistry;

	@Inject
	public DefaultNativeApplicationComponent(ComponentIdentifier identifier, ObjectFactory objects, TaskContainer tasks, DomainObjectEventPublisher eventPublisher, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory) {
		super(identifier, DefaultNativeApplicationVariant.class, objects, tasks, eventPublisher, taskRegistry, taskViewFactory);
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
	public Property<DefaultNativeApplicationVariant> getDevelopmentVariant() {
		return ModelProperties.getProperty(this, "developmentVariant").asProperty(property(of(DefaultNativeApplicationVariant.class)));
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public VariantView<DefaultNativeApplicationVariant> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	@Override
	public Provider<Set<BuildVariant>> getBuildVariants() {
		return ModelProperties.getProperty(this, "buildVariants").as(set(of(BuildVariant.class))).asProvider();
	}

	public void finalizeExtension(Project project) {
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(NativeApplication.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			new CreateNativeBinaryLifecycleTaskRule(taskRegistry).execute(knownVariant);
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(NativeApplication.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			createBinaries((KnownDomainObject<DefaultNativeApplicationVariant>) Cast.uncheckedCast("internal vs public projection", knownVariant));
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(NativeApplication.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			new CreateVariantObjectsLifecycleTaskRule(taskRegistry).execute(knownVariant);
		}));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(NativeApplication.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			new CreateVariantAssembleLifecycleTaskRule(taskRegistry).execute(knownVariant);
		}));
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}
}
