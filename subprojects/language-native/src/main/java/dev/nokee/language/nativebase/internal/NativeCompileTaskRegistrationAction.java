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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.language.base.HasDestinationDirectory;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
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

import static dev.nokee.platform.base.internal.util.PropertyUtils.*;
import static dev.nokee.utils.TaskUtils.configureDescription;

@AutoFactory
public final class NativeCompileTaskRegistrationAction extends ModelActionWithInputs.ModelAction2<LanguageSourceSetIdentifier, ModelState.IsAtLeastRegistered> {
	private final LanguageSourceSetIdentifier identifier;
	private final Class<? extends SourceCompile> publicType;
	private final Class<? extends SourceCompile> implementationType;
	private final ModelRegistry registry;
	private final TaskRegistrationFactory taskRegistrationFactory;
	private final ModelPropertyRegistrationFactory propertyRegistrationFactory;
	private final NativeToolChainSelector toolChainSelector;

	public <T extends SourceCompile> NativeCompileTaskRegistrationAction(LanguageSourceSetIdentifier identifier, Class<T> publicType, Class<? extends T> implementationType, @Provided ModelRegistry registry, @Provided TaskRegistrationFactory taskRegistrationFactory, @Provided ModelPropertyRegistrationFactory propertyRegistrationFactory, @Provided NativeToolChainSelector toolChainSelector) {
		this.identifier = identifier;
		this.publicType = publicType;
		this.implementationType = implementationType;
		this.registry = registry;
		this.taskRegistrationFactory = taskRegistrationFactory;
		this.propertyRegistrationFactory = propertyRegistrationFactory;
		this.toolChainSelector = toolChainSelector;
	}

	@Override
	protected void execute(ModelNode entity, LanguageSourceSetIdentifier identifier, ModelState.IsAtLeastRegistered isAtLeastRegistered) {
		if (identifier.equals(this.identifier)) {
			val compileTask = registry.register(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("compile"), publicType, identifier), implementationType).build());
			compileTask.configure(publicType, configureDescription("Compiles the %s.", identifier));
			compileTask.configure(publicType, configureDestinationDirectory(convention(forObjects(identifier))));
			compileTask.configure(publicType, configureToolChain(convention(selectToolChainUsing(toolChainSelector)).andThen(lockProperty())));
			compileTask.configure(publicType, configureObjectFiles(from(objectFilesInDestinationDirectory())));
			registry.register(propertyRegistrationFactory.create(ModelPropertyIdentifier.of(identifier, "compileTask"), ModelNodes.of(compileTask)));
			entity.addComponent(new NativeCompileTask(compileTask));
		}
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
