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
import dev.nokee.language.base.HasCompileTask;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.utils.FileSystemLocationUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import java.nio.file.Path;
import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.FileCollectionUtils.sourceDirectories;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;
import static dev.nokee.utils.TransformerUtils.transformEach;

public final class AttachHeaderSearchPathsToCompileTaskRule<T> implements Action<T> {
	@Override
	public void execute(T t) {
		if (t instanceof HasCompileTask) {
			if (t instanceof HasHeaders) {
				((HasCompileTask) t).getCompileTask().configure(configureIncludeRoots(from(sourceDirectories(((HasHeaders) t).getHeaders()))));
			}
			if (t instanceof HasHeaderSearchPaths) {
				((HasCompileTask) t).getCompileTask().configure(configureIncludeRoots(from(((HasHeaderSearchPaths) t).getDependentHeaderSearchPaths())));
				((HasCompileTask) t).getCompileTask().configure(task -> {
					assert task instanceof SourceCompile;
					configureCompilerArgs(addAll(asFrameworkSearchPathFlags(((HasHeaderSearchPaths) t).getDependentFrameworkSearchPaths()))).execute((SourceCompile) task);
				});
			}
		}
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

	private static Provider<Iterable<? extends String>> asFrameworkSearchPathFlags(FileCollection frameworksSearchPaths) {
		return frameworksSearchPaths.getElements().map(transformEach(FileSystemLocationUtils::asPath)).map(flatTransformEach(toFrameworkSearchPathFlags()));
	}
	//endregion
}
