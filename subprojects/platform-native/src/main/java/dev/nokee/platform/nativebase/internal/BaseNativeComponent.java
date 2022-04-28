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

import com.google.common.base.Preconditions;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.VariantAwareComponentInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

public abstract class BaseNativeComponent<T extends VariantInternal> extends BaseComponent<T> implements VariantAwareComponentInternal<T> {
	private final Class<T> variantType;
	private final TaskRegistry taskRegistry;
	private final ObjectFactory objects;
	private final ModelRegistry registry;

	public BaseNativeComponent(ComponentIdentifier identifier, Class<T> variantType, ObjectFactory objects, TaskRegistry taskRegistry, ModelRegistry registry) {
		super(identifier, objects);
		this.objects = objects;
		this.registry = registry;
		Preconditions.checkArgument(BaseNativeVariant.class.isAssignableFrom(variantType));
		this.variantType = variantType;
		this.taskRegistry = taskRegistry;
	}

	public abstract NativeComponentDependencies getDependencies();

	protected void createBinaries(KnownDomainObject<T> knownVariant) {
		doCreateBinaries((VariantIdentifier) knownVariant.getIdentifier(), knownVariant);
	}

	private void doCreateBinaries(VariantIdentifier variantIdentifier, KnownDomainObject<T> knownVariant) {
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		final TargetMachine targetMachineInternal = buildVariant.getAxisValue(TARGET_MACHINE_COORDINATE_AXIS);

		if (buildVariant.hasAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
			val incomingDependencies = knownVariant.map(VariantInternal::getResolvableDependencies);
			val objectSourceSets = new NativeLanguageRules(taskRegistry, objects, variantIdentifier).apply(sourceViewOf(this));
			val taskView = knownVariant.map(it -> {
				return ModelProperties.getProperty(it, "tasks").as(ModelType.of(new TypeOf<TaskView<Task>>() {})).get();
			});
			val linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
			if (linkage.isExecutable()) {
				val binaryIdentifier = BinaryIdentifier.of(variantIdentifier, "executable");

				registry.register(ModelRegistration.builder()
					.withComponent(IsBinary.tag())
					.withComponent(ConfigurableTag.tag())
					.withComponent(new IdentifierComponent(binaryIdentifier))
					.withComponent(createdUsing(of(ExecutableBinaryInternal.class), () -> {
						val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkExecutableTask.class, variantIdentifier));
						val binary = objects.newInstance(ExecutableBinaryInternal.class, ModelNodeContext.getCurrentModelNode().get(FullyQualifiedNameComponent.class).get(), binaryIdentifier, objectSourceSets, targetMachineInternal, linkTask, incomingDependencies.get(), taskView.get());
						binary.getBaseName().convention(getBaseName());
						return binary;
					}))
					.build());
			} else if (linkage.isShared()) {
				val binaryIdentifier = BinaryIdentifier.of(variantIdentifier, "sharedLibrary");

				registry.register(ModelRegistration.builder()
					.withComponent(IsBinary.tag())
					.withComponent(ConfigurableTag.tag())
					.withComponent(new IdentifierComponent(binaryIdentifier))
					.withComponent(createdUsing(of(SharedLibraryBinaryInternal.class), () -> {
						val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkSharedLibraryTask.class, variantIdentifier));
						val binary = objects.newInstance(SharedLibraryBinaryInternal.class, ModelNodeContext.getCurrentModelNode().get(FullyQualifiedNameComponent.class).get(), binaryIdentifier, targetMachineInternal, objectSourceSets, linkTask, incomingDependencies.get(), taskView.get());
						binary.getBaseName().convention(getBaseName());
						return binary;
					}))
					.build());
			} else if (linkage.isBundle()) {
				val binaryIdentifier = BinaryIdentifier.of(variantIdentifier, "bundle");

				registry.register(ModelRegistration.builder()
					.withComponent(IsBinary.tag())
					.withComponent(ConfigurableTag.tag())
					.withComponent(new IdentifierComponent(binaryIdentifier))
					.withComponent(createdUsing(of(BundleBinaryInternal.class), () -> {
						val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkBundleTask.class, variantIdentifier));
						val binary = objects.newInstance(BundleBinaryInternal.class, ModelNodeContext.getCurrentModelNode().get(FullyQualifiedNameComponent.class).get(), binaryIdentifier, targetMachineInternal, objectSourceSets, linkTask, incomingDependencies.get(), taskView.get());
						binary.getBaseName().convention(getBaseName());
						return binary;
					}))
					.build());
			} else if (linkage.isStatic()) {
				val binaryIdentifier = BinaryIdentifier.of(variantIdentifier, "staticLibrary");

				registry.register(ModelRegistration.builder()
					.withComponent(IsBinary.tag())
					.withComponent(ConfigurableTag.tag())
					.withComponent(new IdentifierComponent(binaryIdentifier))
					.withComponent(createdUsing(of(StaticLibraryBinaryInternal.class), () -> {
						val createTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("create"), CreateStaticLibraryTask.class, variantIdentifier));
						val binary = objects.newInstance(StaticLibraryBinaryInternal.class, ModelNodeContext.getCurrentModelNode().get(FullyQualifiedNameComponent.class).get(), binaryIdentifier, objectSourceSets, targetMachineInternal, createTask, incomingDependencies.get(), taskView.get());
						binary.getBaseName().convention(getBaseName());
						return binary;
					}))
					.build());
			}
		}

		knownVariant.configure(it -> {
			it.getBinaries().configureEach(NativeBinary.class, binary -> {
				binary.getCompileTasks().configureEach(NativeSourceCompile.class, task -> {
					val taskInternal = (AbstractNativeCompileTask) task;
					sourceViewOf(this).whenElementKnown(NativeHeaderSet.class, knownSourceSet -> {
						taskInternal.getIncludes().from(knownSourceSet.map(LanguageSourceSet::getSourceDirectories));
					});
				});
			});
		});
	}
}
