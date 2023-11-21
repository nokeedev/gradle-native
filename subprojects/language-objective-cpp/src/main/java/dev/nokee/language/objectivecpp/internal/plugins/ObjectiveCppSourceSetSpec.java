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
package dev.nokee.language.objectivecpp.internal.plugins;

import dev.nokee.language.nativebase.NativeSourceSetComponentDependencies;
import dev.nokee.language.nativebase.internal.DefaultNativeSourceSetComponentDependencies;
import dev.nokee.language.nativebase.internal.HasHeaderSearchPaths;
import dev.nokee.language.nativebase.internal.NativeCompileTaskMixIn;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.objectivecpp.tasks.ObjectiveCppCompile;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMixIn;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.tasks.TaskDependency;

import javax.inject.Inject;

public /*final*/ abstract class ObjectiveCppSourceSetSpec extends ModelElementSupport implements ObjectiveCppSourceSet
	, ModelMixIn
	, NativeCompileTaskMixIn<ObjectiveCppCompile, ObjectiveCppCompileTask>
	, DependencyAwareComponentMixIn<NativeSourceSetComponentDependencies, DefaultNativeSourceSetComponentDependencies>
	, HasHeaderSearchPaths
{
	@Inject
	public ObjectiveCppSourceSetSpec(ModelObjectRegistry<DependencyBucket> bucketRegistry) {
		getExtensions().add("headerSearchPaths", bucketRegistry.register(getIdentifier().child("headerSearchPaths"), ResolvableDependencyBucketSpec.class).get());
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.composite(getSource().getBuildDependencies(), getHeaders().getBuildDependencies(), TaskDependencyUtils.of(getCompileTask()));
	}

	@Override
	public ResolvableDependencyBucketSpec getHeaderSearchPaths() {
		return mixedIn("headerSearchPaths");
	}

	@Override
	protected String getTypeName() {
		return "Objective-C++ sources";
	}
}
