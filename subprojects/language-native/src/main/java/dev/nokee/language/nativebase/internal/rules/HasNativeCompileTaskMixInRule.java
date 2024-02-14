/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.language.nativebase.internal.rules;

import dev.nokee.language.base.HasCompileTask;
import dev.nokee.language.base.HasDestinationDirectory;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.language.nativebase.internal.NativeToolChainSelector;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import dev.nokee.platform.base.internal.util.PropertyUtils;
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

import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.lockProperty;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.TaskUtils.configureDescription;

public final class HasNativeCompileTaskMixInRule<T> implements Action<T> {
	private final NativeToolChainSelector toolChainSelector;

	public HasNativeCompileTaskMixInRule(NativeToolChainSelector toolChainSelector) {
		this.toolChainSelector = toolChainSelector;
	}

	@Override
	public void execute(T t) {
		if (t instanceof HasCompileTask && t instanceof ModelElement) {
			((HasCompileTask) t).getCompileTask().configure(configureDescription("Compiles the %s.", t));
			((HasCompileTask) t).getCompileTask().configure(configureDestinationDirectory(convention(forObjects(((ModelElement) t).getIdentifier()))));
			((HasCompileTask) t).getCompileTask().configure(configureToolChain(convention(selectToolChainUsing(toolChainSelector)).andThen(lockProperty())));
			((HasCompileTask) t).getCompileTask().configure(configureObjectFiles(from(objectFilesInDestinationDirectory())));
		}
	}

	//region Destination directory
	private static <SELF extends Task> Action<SELF> configureDestinationDirectory(BiConsumer<? super SELF, ? super PropertyUtils.Property<? extends Directory>> action) {
		return task -> {
			if (task instanceof HasDestinationDirectory) {
				action.accept(task, wrap(((HasDestinationDirectory) task).getDestinationDirectory()));
			}
		};
	}

	private static Function<Task, Provider<Directory>> forObjects(DomainObjectIdentifier identifier) {
		return task -> task.getProject().getLayout().getBuildDirectory().dir("objs/" + OutputDirectoryPath.forIdentifier((ModelObjectIdentifier) identifier));
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
	private static Action<Task> configureObjectFiles(BiConsumer<? super SourceCompile, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> {
			if (task instanceof SourceCompile) {
				action.accept((SourceCompile) task, wrap(objectFilesProperty(task)));
			}
		};
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