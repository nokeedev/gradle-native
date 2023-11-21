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

import dev.nokee.language.nativebase.NativeSourceSetComponentDependencies;
import dev.nokee.language.nativebase.internal.DefaultNativeSourceSetComponentDependencies;
import dev.nokee.language.nativebase.internal.NativeCompileTaskMixIn;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.SwiftCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMixIn;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;

import javax.inject.Inject;

public /*final*/ abstract class SwiftSourceSetSpec extends ModelElementSupport implements SwiftSourceSet
	, ModelMixIn
	, NativeCompileTaskMixIn<SwiftCompile, SwiftCompileTask>
	, DependencyAwareComponentMixIn<NativeSourceSetComponentDependencies>
	, HasImportModules
{
	@Inject
	public SwiftSourceSetSpec(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry) {
		getExtensions().add("importModules", bucketRegistry.register(getIdentifier().child("importModules"), ResolvableDependencyBucketSpec.class).get());
		getExtensions().create("dependencies", DefaultNativeSourceSetComponentDependencies.class, getIdentifier(), bucketRegistry);
		getExtensions().add("compileTask", taskRegistry.register(getIdentifier().child(TaskName.of("compile")), SwiftCompileTask.class).asProvider());
	}

	@Override
	public ResolvableDependencyBucketSpec getImportModules() {
		return mixedIn("importModules");
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.composite(getSource().getBuildDependencies(), TaskDependencyUtils.of(getCompileTask()));
	}

	@Override
	protected String getTypeName() {
		return "Swift sources";
	}
}
