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

import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrarySources;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.internal.rules.CreateVariantAssembleLifecycleTaskRule;
import dev.nokee.runtime.nativebase.TargetMachine;
import groovy.lang.Closure;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;

public class JniLibraryComponentInternal extends BaseComponent<JniLibraryInternal> implements Component
	, DependencyAwareComponent<JavaNativeInterfaceLibraryComponentDependencies>
	, ModelBackedSourceAwareComponentMixIn<JavaNativeInterfaceLibrarySources>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedNamedMixIn
{
	@Getter private final GroupId groupId;
	private final Property<JniLibraryInternal> developmentVariant;
	private final TaskRegistry taskRegistry;

	@Inject
	public JniLibraryComponentInternal(ComponentIdentifier identifier, GroupId groupId, ObjectFactory objects, TaskRegistry taskRegistry) {
		super(identifier, objects);
		this.groupId = groupId;
		this.developmentVariant = objects.property(JniLibraryInternal.class);
		this.taskRegistry = taskRegistry;
	}

	public SetProperty<TargetMachine> getTargetMachines() {
		return ModelProperties.getProperty(this, "targetMachines").as(SetProperty.class).get();
	}

	@Override
	public JavaNativeInterfaceLibraryComponentDependencies getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as(JavaNativeInterfaceLibraryComponentDependencies.class).get();
	}

	@Override
	public void dependencies(Action<? super JavaNativeInterfaceLibraryComponentDependencies> action) {
		action.execute(getDependencies());
	}

	@Override
	public void dependencies(@SuppressWarnings("rawtypes") Closure closure) {
		dependencies(ConfigureUtil.configureUsing(closure));
	}

	//region Variant-awareness
	public VariantView<JniLibraryInternal> getVariants() {
		return ModelProperties.getProperty(this, "variants").as(VariantView.class).get();
	}
	//endregion

	public Configuration getJvmImplementationDependencies() {
		return getDependencies().getJvmImplementation().getAsConfiguration();
	}

	@Override
	public Property<JniLibraryInternal> getDevelopmentVariant() {
		return developmentVariant;
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public VariantCollection<JniLibraryInternal> getVariantCollection() {
		throw new UnsupportedOperationException("Use 'variants' property instead.");
	}

	@Override
	public SetProperty<BuildVariant> getBuildVariants() {
		return ModelProperties.getProperty(this, "buildVariants").as(SetProperty.class).get();
	}

	public void finalizeValue() {
		whenElementKnown(this, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofProjection(JniLibrary.class).asKnownObject(), (entity, variantIdentifier, knownVariant) -> {
			new CreateVariantAssembleLifecycleTaskRule(taskRegistry).accept(knownVariant);
		}));
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}
}
