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
package dev.nokee.language.swift.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.internal.DependentFrameworkSearchPaths;
import dev.nokee.language.nativebase.internal.NativeCompileTask;
import dev.nokee.language.swift.tasks.SwiftCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Provider;

import java.nio.file.Path;
import java.util.function.BiConsumer;

import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;

final class AttachImportModulesToCompileTaskRule extends ModelActionWithInputs.ModelAction3<DependentImportModules, DependentFrameworkSearchPaths, NativeCompileTask> {
	private final ModelRegistry registry;

	public AttachImportModulesToCompileTaskRule(ModelRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, DependentImportModules incomingModules, DependentFrameworkSearchPaths incomingFrameworks, NativeCompileTask compileTask) {
		registry.instantiate(configure(compileTask.get().getId(), SwiftCompile.class, configureImportModules(from(incomingModules))));
		registry.instantiate(configure(compileTask.get().getId(), SwiftCompile.class, configureCompilerArgs(addAll(asFrameworkSearchPathFlags(incomingFrameworks)))));
	}

	//region Import modules
	public static Action<Task> configureImportModules(BiConsumer<? super Task, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(importModulesProperty(task)));
	}

	private static ConfigurableFileCollection importModulesProperty(Task task) {
		if (task instanceof SwiftCompileTask) {
			return ((SwiftCompileTask) task).getModules();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion

	//region Compiler arguments
	private static Action<SourceCompile> configureCompilerArgs(BiConsumer<? super SourceCompile, ? super PropertyUtils.CollectionProperty<String>> action) {
		return task -> action.accept(task, wrap(task.getCompilerArgs()));
	}

	private static Transformer<Iterable<String>, Path> toFrameworkSearchPathFlags() {
		return it -> ImmutableList.of("-F", it.toString());
	}

	private static Provider<Iterable<String>> asFrameworkSearchPathFlags(DependentFrameworkSearchPaths frameworksSearchPaths) {
		return frameworksSearchPaths.getAsProvider().map(flatTransformEach(toFrameworkSearchPathFlags()));
	}
	//endregion
}
