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

import com.google.common.collect.ImmutableList;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelNodeBackedKnownDomainObject;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.*;
import dev.nokee.utils.Cast;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;

public class DefaultNativeApplicationComponent extends BaseNativeComponent<DefaultNativeApplicationVariant> implements DependencyAwareComponent<NativeApplicationComponentDependencies>, BinaryAwareComponent, Component, SourceAwareComponent<ComponentSources> {
	private final DefaultNativeApplicationComponentDependencies dependencies;
	private final TaskRegistry taskRegistry;
	private final Supplier<NativeApplicationComponentVariants> componentVariants;
	private final BinaryView<Binary> binaries;

	@Inject
	public DefaultNativeApplicationComponent(ComponentIdentifier<?> identifier, ObjectFactory objects, TaskContainer tasks, DomainObjectEventPublisher eventPublisher, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory, ModelLookup modelLookup) {
		super(identifier, DefaultNativeApplicationVariant.class, objects, tasks, eventPublisher, taskRegistry, taskViewFactory);
		this.taskRegistry = taskRegistry;
		val path = ModelNodeUtils.getPath(getNode());
		this.dependencies = ModelNodeUtils.get(modelLookup.get(path.child("dependencies")), DefaultNativeApplicationComponentDependencies.class);
		this.componentVariants = () -> ModelNodeUtils.get(ModelNodes.of(this), ModelType.of(NativeApplicationComponentVariants.class));
		this.binaries = (BinaryView<Binary>) ModelNodeUtils.get(modelLookup.get(path.child("binaries")), BinaryView.class);
	}

	@Override
	public DefaultNativeApplicationComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public Property<DefaultNativeApplicationVariant> getDevelopmentVariant() {
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

	@Override
	public VariantView<DefaultNativeApplicationVariant> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}

	@Override
	public SetProperty<BuildVariantInternal> getBuildVariants() {
		return getComponentVariants().getBuildVariants();
	}

	private NativeApplicationComponentVariants getComponentVariants() {
		return componentVariants.get();
	}

	public void finalizeExtension(Project project) {
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(NativeApplication.class)), (entity, variantIdentifier, variantProjection) -> {
			new CreateNativeBinaryLifecycleTaskRule(taskRegistry).accept(new ModelNodeBackedKnownDomainObject<>(ModelType.of(NativeApplication.class), entity));
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(NativeApplication.class)), (entity, variantIdentifier, variantProjection) -> {
			createBinaries((KnownDomainObject<DefaultNativeApplicationVariant>) Cast.uncheckedCast("", new ModelNodeBackedKnownDomainObject<>(ModelType.of(NativeApplication.class), entity)));
		}));
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(NativeApplication.class)), (entity, variantIdentifier, variantProjection) -> {
			new CreateVariantObjectsLifecycleTaskRule(taskRegistry).accept(new ModelNodeBackedKnownDomainObject<>(ModelType.of(NativeApplication.class), entity));
		}));
		new CreateVariantAwareComponentObjectsLifecycleTaskRule(taskRegistry).execute(this);
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(NativeApplication.class)), (entity, variantIdentifier, variantProjection) -> {
			new CreateVariantAssembleLifecycleTaskRule(taskRegistry).accept(new ModelNodeBackedKnownDomainObject<>(ModelType.of(NativeApplication.class), entity));
		}));
		new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry).execute(this);
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}
}
