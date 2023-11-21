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
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.nativebase.internal.HasApiElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.HasLinkElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.HasRuntimeElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasApiDependencyBucket;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public /*final*/ abstract class DefaultNativeLibraryVariant extends BaseVariant implements NativeLibrary, VariantInternal
	, NativeVariant
	, NativeSourcesAware
	, DependencyAwareComponentMixIn<NativeLibraryComponentDependencies, DefaultNativeLibraryComponentDependencies>
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, AssembleTaskMixIn
	, HasApiDependencyBucket
	, HasApiElementsDependencyBucket
	, HasLinkElementsDependencyBucket
	, HasRuntimeElementsDependencyBucket
{
	@Inject
	public DefaultNativeLibraryVariant(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry, Factory<SourceView<LanguageSourceSet>> sourcesFactory) {
		getExtensions().add("api", bucketRegistry.register(getIdentifier().child("api"), DeclarableDependencyBucketSpec.class).get());
		getExtensions().add("runtimeElements", bucketRegistry.register(getIdentifier().child("runtimeElements"), ConsumableDependencyBucketSpec.class).get());
		getExtensions().add("linkElements", bucketRegistry.register(getIdentifier().child("linkElements"), ConsumableDependencyBucketSpec.class).get());
		getExtensions().add("apiElements", bucketRegistry.register(getIdentifier().child("apiElements"), ConsumableDependencyBucketSpec.class).get());
		getExtensions().add("sources", sourcesFactory.create());
		getExtensions().add("objectsTask", taskRegistry.register(getIdentifier().child(TaskName.of("objects")), Task.class).asProvider());

		if (getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).isShared()) {
			getExtensions().add("binaryLifecycleTask", taskRegistry.register(getIdentifier().child(TaskName.of("sharedLibrary")), Task.class).asProvider());
		} else {
			getExtensions().add("binaryLifecycleTask", taskRegistry.register(getIdentifier().child(TaskName.of("staticLibrary")), Task.class).asProvider());
		}
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return BinaryAwareComponentMixIn.super.getBinaries();
	}

	@Override
	public DeclarableDependencyBucketSpec getApi() {
		return (DeclarableDependencyBucketSpec) getExtensions().getByName("api");
	}

	@Override
	public ConsumableDependencyBucketSpec getApiElements() {
		return (ConsumableDependencyBucketSpec) getExtensions().getByName("apiElements");
	}

	@Override
	public ConsumableDependencyBucketSpec getLinkElements() {
		return (ConsumableDependencyBucketSpec) getExtensions().getByName("linkElements");
	}

	@Override
	public ConsumableDependencyBucketSpec getRuntimeElements() {
		return (ConsumableDependencyBucketSpec) getExtensions().getByName("runtimeElements");
	}

	@SuppressWarnings("unchecked")
	public NamedDomainObjectProvider<? extends NativeBinary> getSharedOrStaticLibraryBinary() {
		NamedDomainObjectProvider<? extends NativeBinary> result = (NamedDomainObjectProvider<? extends NativeBinary>) getExtensions().findByName("sharedLibrary");
		if (result == null) {
			result = (NamedDomainObjectProvider<? extends NativeBinary>) getExtensions().findByName("staticLibrary");
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public TaskProvider<Task> getBinaryLifecycleTask() {
		return (TaskProvider<Task>) getExtensions().getByName("binaryLifecycleTask");
	}

	@SuppressWarnings("unchecked")
	public TaskProvider<Task> getObjectsTask() {
		return (TaskProvider<Task>) getExtensions().getByName("objectsTask");
	}

	@Override
	protected String getTypeName() {
		return "native library";
	}
}
