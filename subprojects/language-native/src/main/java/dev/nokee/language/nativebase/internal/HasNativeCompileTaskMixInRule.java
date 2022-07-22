/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.HasDestinationDirectory;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.toolchain.NativeToolChain;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.nokee.model.internal.DomainObjectEntities.newEntity;
import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.lockProperty;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.TaskUtils.configureDescription;

public final class HasNativeCompileTaskMixInRule extends ModelActionWithInputs.ModelAction3<NativeCompileTypeComponent, IdentifierComponent, ModelComponentTag<HasNativeCompileTaskMixIn.Tag>> {
	private final ModelRegistry registry;
	private final NativeToolChainSelector toolChainSelector;

	public HasNativeCompileTaskMixInRule(ModelRegistry registry, NativeToolChainSelector toolChainSelector) {
		this.registry = registry;
		this.toolChainSelector = toolChainSelector;
	}

	@Override
	protected void execute(ModelNode entity, NativeCompileTypeComponent knownObject, IdentifierComponent identifier, ModelComponentTag<HasNativeCompileTaskMixIn.Tag> ignored) {
		val implementationType = knownObject.getNativeCompileTaskType();

		val compileTask = ModelNodes.of(registry.register(newEntity("compile", implementationType, it -> it.ownedBy(entity))));
		registry.instantiate(configure(compileTask.getId(), implementationType, configureDescription("Compiles the %s.", identifier.get())));
		registry.instantiate(configure(compileTask.getId(), implementationType, configureDestinationDirectory(convention(forObjects(identifier.get())))));
		registry.instantiate(configure(compileTask.getId(), implementationType, configureToolChain(convention(selectToolChainUsing(toolChainSelector)).andThen(lockProperty()))));
		registry.instantiate(configure(compileTask.getId(), implementationType, configureObjectFiles(from(objectFilesInDestinationDirectory()))));
		entity.addComponent(new NativeCompileTask(compileTask));
	}

	//region Destination directory
	private static <SELF extends Task & HasDestinationDirectory> Action<SELF> configureDestinationDirectory(BiConsumer<? super SELF, ? super PropertyUtils.Property<? extends Directory>> action) {
		return task -> action.accept(task, wrap(task.getDestinationDirectory()));
	}

	private static Function<Task, Provider<Directory>> forObjects(DomainObjectIdentifier identifier) {
		return task -> task.getProject().getLayout().getBuildDirectory().dir("objs/" + OutputDirectoryPath.fromIdentifier(identifier));
	}
	//endregion

	//region Toolchain
	private static Action<Task> configureToolChain(BiConsumer<? super Task, ? super PropertyUtils.Property<? extends NativeToolChain>> action) {
		return task -> action.accept(task, wrap(toolChainProperty(task)));
	}

	private static Function<Task, Object> selectToolChainUsing(NativeToolChainSelector toolChainSelector) {
		Objects.requireNonNull(toolChainSelector);
		return toolChainSelector::select;
	}

	private static Property<NativeToolChain> toolChainProperty(Task task) {
		if (task instanceof AbstractNativeCompileTask) {
			return ((AbstractNativeCompileTask) task).getToolChain();
		} else if (task instanceof SwiftCompile) {
			return ((SwiftCompile) task).getToolChain();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion

	//region Object files
	private static Action<SourceCompile> configureObjectFiles(BiConsumer<? super SourceCompile, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(objectFilesProperty(task)));
	}

	private static ConfigurableFileCollection objectFilesProperty(Task task) {
		if (task instanceof HasObjectFiles) {
			return ((HasObjectFiles) task).getObjectFiles();
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static Function<SourceCompile, Object> objectFilesInDestinationDirectory() {
		return task -> task.getDestinationDirectory().map(toObjectFiles());
	}

	private static Transformer<Object, Directory> toObjectFiles() {
		return objectFileDirectory -> objectFileDirectory.getAsFileTree().matching(pattern -> pattern.include("**/*.o", "**/*.obj"));
	}
	//endregion
}
