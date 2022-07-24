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

import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.archiving.HasCreateTask;
import dev.nokee.platform.nativebase.internal.archiving.NativeArchiveTask;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.Buildable;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public class StaticLibraryBinaryInternal extends BaseNativeBinary implements StaticLibraryBinary
	, Buildable
	, HasPublicType
	, ModelBackedNamedMixIn
	, ModelBackedHasBaseNameMixIn
	, HasCreateTask
	, HasObjectFilesToBinaryTask
{
	@Inject
	public StaticLibraryBinaryInternal(BinaryIdentifier identifier, ObjectFactory objects, ProviderFactory providers) {
		super(identifier, objects, providers);
	}

	@Override
	public TaskProvider<CreateStaticLibrary> getCreateTask() {
		return (TaskProvider<CreateStaticLibrary>) ModelElements.of(this, NativeArchiveTask.class).as(CreateStaticLibrary.class).asProvider();
	}

	@Override
	public TaskProvider<CreateStaticLibraryTask> getCreateOrLinkTask() {
		return (TaskProvider<CreateStaticLibraryTask>) ModelElements.of(this, NativeArchiveTask.class).as(CreateStaticLibraryTask.class).asProvider();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.of(getCreateOrLinkTask());
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(StaticLibraryBinary.class);
	}

	@Override
	public String toString() {
		return "static library binary '" + getName() + "'";
	}
}
