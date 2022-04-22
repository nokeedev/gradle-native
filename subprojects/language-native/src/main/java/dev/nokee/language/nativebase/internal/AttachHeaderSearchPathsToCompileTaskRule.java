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

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import java.nio.file.Path;
import java.util.function.BiConsumer;

import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;

public final class AttachHeaderSearchPathsToCompileTaskRule extends ModelActionWithInputs.ModelAction4<DependentHeaderSearchPaths, DependentFrameworkSearchPaths, ProjectHeaderSearchPaths, NativeCompileTask> {
	private final ModelRegistry registry;

	public AttachHeaderSearchPathsToCompileTaskRule(ModelRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, DependentHeaderSearchPaths incomingHeaders, DependentFrameworkSearchPaths incomingFrameworks, ProjectHeaderSearchPaths userHeaders, NativeCompileTask compileTask) {
		registry.instantiate(configure(compileTask.get().getId(), NativeSourceCompile.class, configureIncludeRoots(from(userHeaders).andThen(from(incomingHeaders)))));
		registry.instantiate(configure(compileTask.get().getId(), NativeSourceCompile.class, configureCompilerArgs(addAll(asFrameworkSearchPathFlags(incomingFrameworks)))));
	}

	//region Includes
	private static <SELF extends Task> Action<SELF> configureIncludeRoots(BiConsumer<? super SELF, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(includesProperty(task)));
	}

	private static ConfigurableFileCollection includesProperty(Task task) {
		if (task instanceof AbstractNativeCompileTask) {
			return ((AbstractNativeCompileTask) task).getIncludes();
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
