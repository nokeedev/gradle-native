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

package dev.nokee.platform.c.internal;

import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.internal.IVariantOf;
import dev.nokee.platform.base.internal.ParentAware;
import dev.nokee.platform.base.internal.VariantComponentSpec;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.internal.HasBinaryLifecycleTask;
import dev.nokee.platform.nativebase.internal.INativeComponentSpec;
import dev.nokee.platform.nativebase.internal.NativeVariantSpec;
import dev.nokee.platform.nativebase.internal.mixins.ApiElementsDependencyBucketMixIn;
import dev.nokee.platform.nativebase.internal.mixins.LinkElementsDependencyBucketMixIn;
import dev.nokee.platform.nativebase.internal.mixins.RuntimeElementsDependencyBucketMixIn;
import dev.nokee.platform.nativebase.internal.plugins.NativePlatformPluginSupport;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract /*final*/ class DefaultCLibrary extends CLibrarySpec implements INativeComponentSpec<CLibrarySpec>
	, VariantComponentSpec<DefaultCLibrary.Variant>
{
	public static abstract /*final*/ class Variant extends CLibrarySpec implements IVariantOf<CLibrarySpec>
		, NativeVariantSpec
		, NativeSourcesAware
		, ApiElementsDependencyBucketMixIn
		, LinkElementsDependencyBucketMixIn
		, RuntimeElementsDependencyBucketMixIn
		, HasBinaryLifecycleTask
		, NativePlatformPluginSupport.VariantOf<DefaultCLibrary>
		, ParentAware
	{
		@Inject
		public Variant(ModelObjectRegistry<Task> taskRegistry) {
			if (getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).isShared()) {
				getExtensions().add("binaryLifecycleTask", taskRegistry.register(getIdentifier().child(TaskName.of("sharedLibrary")), Task.class).asProvider());
			} else {
				getExtensions().add("binaryLifecycleTask", taskRegistry.register(getIdentifier().child(TaskName.of("staticLibrary")), Task.class).asProvider());
			}
		}

		public VariantIdentifier getIdentifier() {
			return (VariantIdentifier) super.getIdentifier();
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
	}
}
