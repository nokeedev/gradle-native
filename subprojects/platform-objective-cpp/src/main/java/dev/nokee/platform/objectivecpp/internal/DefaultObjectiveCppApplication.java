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

package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.IVariantOf;
import dev.nokee.platform.base.internal.VariantComponentSpec;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.nativebase.internal.HasBinaryLifecycleTask;
import dev.nokee.platform.nativebase.internal.INativeComponentSpec;
import dev.nokee.platform.nativebase.internal.NativeExecutableBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeVariant;
import dev.nokee.platform.nativebase.internal.mixins.RuntimeElementsDependencyBucketMixIn;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public /*final*/ abstract class DefaultObjectiveCppApplication extends ObjectiveCppApplicationSpec implements INativeComponentSpec<ObjectiveCppApplicationSpec>
	, VariantComponentSpec<DefaultObjectiveCppApplication.Variant>
{
	public static abstract class Variant extends ObjectiveCppApplicationSpec implements IVariantOf<ObjectiveCppApplicationSpec>, VariantInternal
		, NativeVariant
		, NativeSourcesAware
		, RuntimeElementsDependencyBucketMixIn
		, HasBinaryLifecycleTask
	{
		@Inject
		public Variant(ModelObjectRegistry<Task> taskRegistry) {
			getExtensions().add("binaryLifecycleTask", taskRegistry.register(getIdentifier().child(TaskName.of("executable")), Task.class).asProvider());
		}

		public VariantIdentifier getIdentifier() {
			return (VariantIdentifier) super.getIdentifier();
		}

		public BuildVariantInternal getBuildVariant() {
			return (BuildVariantInternal) getIdentifier().getBuildVariant();
		}

		@SuppressWarnings("unchecked")
		public NamedDomainObjectProvider<NativeExecutableBinarySpec> getExecutable() {
			return (NamedDomainObjectProvider<NativeExecutableBinarySpec>) getExtensions().getByName("executable");
		}

		@SuppressWarnings("unchecked")
		public TaskProvider<Task> getBinaryLifecycleTask() {
			return (TaskProvider<Task>) getExtensions().getByName("binaryLifecycleTask");
		}
	}
}
