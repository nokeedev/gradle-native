/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.testing.xctest.internal;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.nativebase.internal.HasRuntimeElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.NativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public /*final*/ abstract class DefaultXCTestTestSuiteVariant extends BaseVariant implements IosApplication, VariantInternal
	, NativeVariant
	, NativeSourcesAware
	, DependencyAwareComponentMixIn<NativeComponentDependencies, DefaultNativeComponentDependencies>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, AssembleTaskMixIn
	, HasRuntimeElementsDependencyBucket
{
	@Inject
	public DefaultXCTestTestSuiteVariant(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry, Factory<SourceView<LanguageSourceSet>> sourcesFactory) {
		getExtensions().add("runtimeElements", bucketRegistry.register(getIdentifier().child("runtimeElements"), ConsumableDependencyBucketSpec.class).get());
		getExtensions().add("sources", sourcesFactory.create());
		getExtensions().add("objectsTask", taskRegistry.register(getIdentifier().child(TaskName.of("objects")), Task.class).asProvider());
	}

	@Override
	public ConsumableDependencyBucketSpec getRuntimeElements() {
		return (ConsumableDependencyBucketSpec) getExtensions().getByName("runtimeElements");
	}

	@SuppressWarnings("unchecked")
	public TaskProvider<Task> getObjectsTask() {
		return (TaskProvider<Task>) getExtensions().getByName("objectsTask");
	}

	@Override
	protected String getTypeName() {
		return "XCTest test suite";
	}
}
