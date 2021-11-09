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
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
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
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;

public class DefaultNativeLibraryComponent extends BaseNativeComponent<DefaultNativeLibraryVariant> implements Component
	, DependencyAwareComponent<NativeLibraryComponentDependencies>
	, ModelBackedSourceAwareComponentMixIn<ComponentSources>
	, ModelBackedVariantAwareComponentMixIn<DefaultNativeLibraryVariant>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedHasDevelopmentVariantMixIn<DefaultNativeLibraryVariant>
	, ModelBackedNamedMixIn
{
	private final TaskRegistry taskRegistry;

	@Inject
	public DefaultNativeLibraryComponent(ComponentIdentifier identifier, ObjectFactory objects, TaskContainer tasks, DomainObjectEventPublisher eventPublisher, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory) {
		super(identifier, DefaultNativeLibraryVariant.class, objects, tasks, eventPublisher, taskRegistry, taskViewFactory);
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
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return ModelProperties.getProperty(this, "buildVariants").as(SetProperty.class).get();
	}

	@Override
	public Property<DefaultNativeLibraryVariant> getDevelopmentVariant() {
		return ModelProperties.getProperty(this, "developmentVariant").as(Property.class).get();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public VariantView<DefaultNativeLibraryVariant> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	@Override
	public VariantCollection<DefaultNativeLibraryVariant> getVariantCollection() {
		throw new UnsupportedOperationException("Use 'variants' property instead.");
	}

	public void finalizeExtension(Project project) {
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(NativeLibrary.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			new CreateNativeBinaryLifecycleTaskRule(taskRegistry).accept(knownVariant);
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(NativeLibrary.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			createBinaries((KnownDomainObject<DefaultNativeLibraryVariant>) Cast.uncheckedCast("internal vs public projection", knownVariant));
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(NativeLibrary.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			new CreateVariantObjectsLifecycleTaskRule(taskRegistry).accept(knownVariant);
		}));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(NativeLibrary.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			new CreateVariantAssembleLifecycleTaskRule(taskRegistry).accept(knownVariant);
		}));
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}
}
