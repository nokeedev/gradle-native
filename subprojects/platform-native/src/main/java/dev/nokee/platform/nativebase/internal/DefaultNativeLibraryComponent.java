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
import dev.nokee.model.internal.registry.ModelNodeBackedKnownDomainObject;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantCollection;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.*;
import dev.nokee.utils.Cast;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;

public class DefaultNativeLibraryComponent extends BaseNativeComponent<DefaultNativeLibraryVariant> implements DependencyAwareComponent<NativeLibraryComponentDependencies>, BinaryAwareComponent, Component, SourceAwareComponent<ComponentSources> {
	private final TaskRegistry taskRegistry;
	private final SetProperty<BuildVariantInternal> buildVariants;
	private final Property<DefaultNativeLibraryVariant> developmentVariant;

	@Inject
	public DefaultNativeLibraryComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, TaskContainer tasks, DomainObjectEventPublisher eventPublisher, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory) {
		super(identifier, DefaultNativeLibraryVariant.class, objects, tasks, eventPublisher, taskRegistry, taskViewFactory);
		this.buildVariants = objects.setProperty(BuildVariantInternal.class);
		this.developmentVariant = objects.property(DefaultNativeLibraryVariant.class);
		this.taskRegistry = taskRegistry;
	}

	@Override
	public DefaultNativeLibraryComponentDependencies getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as(DefaultNativeLibraryComponentDependencies.class).get();
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return buildVariants;
	}

	@Override
	public Property<DefaultNativeLibraryVariant> getDevelopmentVariant() {
		return developmentVariant;
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
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(NativeLibrary.class)), (entity, variantIdentifier, variantProjection) -> {
			new CreateNativeBinaryLifecycleTaskRule(taskRegistry).accept(new ModelNodeBackedKnownDomainObject<>(ModelType.of(NativeLibrary.class), entity));
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(NativeLibrary.class)), (entity, variantIdentifier, variantProjection) -> {
			createBinaries((KnownDomainObject<DefaultNativeLibraryVariant>) Cast.uncheckedCast("", new ModelNodeBackedKnownDomainObject<>(ModelType.of(NativeLibrary.class), entity)));
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(NativeLibrary.class)), (entity, variantIdentifier, variantProjection) -> {
			new CreateVariantObjectsLifecycleTaskRule(taskRegistry).accept(new ModelNodeBackedKnownDomainObject<>(ModelType.of(NativeLibrary.class), entity));
		}));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(NativeLibrary.class)), (entity, variantIdentifier, variantProjection) -> {
			new CreateVariantAssembleLifecycleTaskRule(taskRegistry).accept(new ModelNodeBackedKnownDomainObject<>(ModelType.of(NativeLibrary.class), entity));
		}));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}
}
