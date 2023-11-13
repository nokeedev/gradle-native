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
package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.language.nativebase.internal.DefaultCompilableNativeComponentDependencies;
import dev.nokee.language.nativebase.internal.HasHeaderSearchPaths;
import dev.nokee.language.nativebase.internal.NativeCompileTaskMixIn;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;

import javax.inject.Inject;

public /*final*/ abstract class ObjectiveCSourceSetSpec extends ModelElementSupport implements ObjectiveCSourceSet
	, NativeCompileTaskMixIn<ObjectiveCCompileTask>
	, DependencyAwareComponentMixIn<DefaultCompilableNativeComponentDependencies>
	, HasHeaderSearchPaths
{
	@Inject
	public ObjectiveCSourceSetSpec(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry) {
		getExtensions().create("dependencies", DefaultCompilableNativeComponentDependencies.class, getIdentifier(), bucketRegistry);
		getExtensions().add("compileTask", taskRegistry.register(getIdentifier().child(TaskName.of("compile")), ObjectiveCCompileTask.class).asProvider());
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.composite(getSource().getBuildDependencies(), getHeaders().getBuildDependencies(), TaskDependencyUtils.of(getCompileTask()));
	}

	@Override
	public ResolvableDependencyBucketSpec getHeaderSearchPaths() {
		return getDependencies().getHeaderSearchPaths();
	}

	@Override
	public String toString() {
		return "Objective-C sources '" + getName() + "'";
	}
}
