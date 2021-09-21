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
import dev.nokee.model.internal.DomainObjectCreated;
import dev.nokee.model.internal.DomainObjectDiscovered;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import dev.nokee.platform.base.internal.variants.VariantViewInternal;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;

public abstract class BaseNativeComponent<T extends VariantInternal> extends BaseComponent<T> implements VariantAwareComponentInternal<T> {
	private final Class<T> variantType;
	private final TaskRegistry taskRegistry;
	private final ObjectFactory objects;
	private final DomainObjectEventPublisher eventPublisher;
	private final TaskViewFactory taskViewFactory;

	public BaseNativeComponent(ComponentIdentifier<?> identifier, Class<T> variantType, ObjectFactory objects, TaskContainer tasks, DomainObjectEventPublisher eventPublisher, TaskRegistry taskRegistry, TaskViewFactory taskViewFactory) {
		super(identifier, objects);
		this.objects = objects;
		this.eventPublisher = eventPublisher;
		this.taskViewFactory = taskViewFactory;
		Preconditions.checkArgument(BaseNativeVariant.class.isAssignableFrom(variantType));
		this.variantType = variantType;
		this.taskRegistry = taskRegistry;
	}

	public abstract NativeComponentDependencies getDependencies();

	public VariantViewInternal<T> getVariants() {
		return getVariantCollection().getAsView(variantType);
	}

	protected void createBinaries(KnownVariant<T> knownVariant) {
		val variantIdentifier = knownVariant.getIdentifier();
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		final TargetMachine targetMachineInternal = buildVariant.getAxisValue(TARGET_MACHINE_COORDINATE_AXIS);

		if (buildVariant.hasAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
			val linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
			if (linkage.isExecutable()) {
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("executable"), ExecutableBinaryInternal.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifier));
			} else if (linkage.isShared()) {
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifier));
			} else if (linkage.isBundle()) {
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("bundle"), BundleBinaryInternal.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifier));
			} else if (linkage.isStatic()) {
				val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("staticLibrary"), StaticLibraryBinaryInternal.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(binaryIdentifier));
			}
		}

		knownVariant.configure(it -> {
			val incomingDependencies = (NativeIncomingDependencies) it.getResolvableDependencies();
			val objectSourceSets = new NativeLanguageRules(taskRegistry, objects, variantIdentifier).apply(sourceViewOf(this));
			BaseNativeVariant variantInternal = (BaseNativeVariant)it;
			if (buildVariant.hasAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
				val linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
				if (linkage.isExecutable()) {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("executable"), ExecutableBinaryInternal.class, variantIdentifier);

					// Binary factory
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkExecutableTask.class, variantIdentifier));
					val binary = objects.newInstance(ExecutableBinaryInternal.class, binaryIdentifier, objectSourceSets, targetMachineInternal, linkTask, incomingDependencies, taskViewFactory);
					eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, binary));

					binary.getBaseName().convention(getBaseName());
				} else if (linkage.isShared()) {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, variantIdentifier);

					// Binary factory
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkSharedLibraryTask.class, variantIdentifier));
					val binary = objects.newInstance(SharedLibraryBinaryInternal.class, binaryIdentifier, targetMachineInternal, objectSourceSets, linkTask, incomingDependencies, taskViewFactory);
					eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, binary));

					binary.getBaseName().convention(getBaseName());
				} else if (linkage.isBundle()) {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("bundle"), BundleBinaryInternal.class, variantIdentifier);

					// Binary factory
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkBundleTask.class, variantIdentifier));
					val binary = objects.newInstance(BundleBinaryInternal.class, binaryIdentifier, targetMachineInternal, objectSourceSets, linkTask, incomingDependencies, taskViewFactory);
					eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, binary));

					binary.getBaseName().convention(getBaseName());
				} else if (linkage.isStatic()) {
					val binaryIdentifier = BinaryIdentifier.of(BinaryName.of("staticLibrary"), StaticLibraryBinaryInternal.class, variantIdentifier);

					// Binary factory
					val createTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("create"), CreateStaticLibraryTask.class, variantIdentifier));
					val binary = objects.newInstance(StaticLibraryBinaryInternal.class, binaryIdentifier, objectSourceSets, targetMachineInternal, createTask, incomingDependencies, taskViewFactory);
					eventPublisher.publish(new DomainObjectCreated<>(binaryIdentifier, binary));

					binary.getBaseName().convention(getBaseName());
				}
			}
			it.getBinaries().configureEach(NativeBinary.class, binary -> {
				binary.getCompileTasks().configureEach(NativeSourceCompile.class, task -> {
					val taskInternal = (AbstractNativeCompileTask) task;
					sourceViewOf(this).whenElementKnownEx(NativeHeaderSet.class, knownSourceSet -> {
						taskInternal.getIncludes().from(knownSourceSet.map(LanguageSourceSet::getSourceDirectories));
					});
				});
			});
		});
	}
}
