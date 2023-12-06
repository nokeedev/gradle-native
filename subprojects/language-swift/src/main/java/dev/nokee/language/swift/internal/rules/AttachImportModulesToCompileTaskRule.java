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
package dev.nokee.language.swift.internal.rules;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.swift.internal.SwiftSourceSetSpec;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.utils.FileSystemLocationUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;
import static dev.nokee.utils.TransformerUtils.transformEach;

public final class AttachImportModulesToCompileTaskRule implements Action<SwiftSourceSetSpec> {
	@Override
	public void execute(SwiftSourceSetSpec sourceSet) {
		sourceSet.getCompileTask().configure(configureImportModules(from(sourceSet.getDependentImportModules())));
		sourceSet.getCompileTask().configure(configureCompilerArgs(addAll(asFrameworkSearchPathFlags(sourceSet.getDependentFrameworkSearchPaths().getElements()))));
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

	private static Provider<Iterable<? extends String>> asFrameworkSearchPathFlags(Provider<Set<FileSystemLocation>> frameworksSearchPaths) {
		return frameworksSearchPaths.map(transformEach(FileSystemLocationUtils::asPath)).map(flatTransformEach(toFrameworkSearchPathFlags()));
	}
	//endregion
}
