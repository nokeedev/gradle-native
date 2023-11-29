/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.nativebase.internal.HasRuntimeElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public /*final*/ abstract class DefaultNativeApplicationVariant extends BaseVariant implements NativeApplication, VariantInternal
	, NativeVariantSpec
	, NativeSourcesAware
	, DependencyAwareComponentMixIn<NativeApplicationComponentDependencies>
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, AssembleTaskMixIn
	, HasRuntimeElementsDependencyBucket
{
	@Inject
	public DefaultNativeApplicationVariant(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry, Factory<SourceView<LanguageSourceSet>> sourcesFactory) {
		getExtensions().add("runtimeElements", bucketRegistry.register(getIdentifier().child("runtimeElements"), ConsumableDependencyBucketSpec.class).get());
		getExtensions().add("sources", sourcesFactory.create());
		getExtensions().add("binaryLifecycleTask", taskRegistry.register(getIdentifier().child(TaskName.of("executable")), Task.class).asProvider());
		getExtensions().add("objectsTask", taskRegistry.register(getIdentifier().child(TaskName.of("objects")), Task.class).asProvider());
	}

	@Override
	@NestedObject
	public abstract DefaultNativeApplicationComponentDependencies getDependencies();

	@SuppressWarnings("unchecked")
	public NamedDomainObjectProvider<NativeExecutableBinarySpec> getExecutable() {
		return (NamedDomainObjectProvider<NativeExecutableBinarySpec>) getExtensions().getByName("executable");
	}

	@SuppressWarnings("unchecked")
	public TaskProvider<Task> getBinaryLifecycleTask() {
		return (TaskProvider<Task>) getExtensions().getByName("binaryLifecycleTask");

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
		return "native application";
	}
}
