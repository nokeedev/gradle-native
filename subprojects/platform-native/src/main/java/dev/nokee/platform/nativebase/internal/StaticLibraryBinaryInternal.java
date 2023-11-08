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

import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.archiving.CreateTaskMixIn;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

@DomainObjectEntities.Tag({IsBinary.class, ConfigurableTag.class, NativeLanguageSourceSetAwareTag.class})
public /*final*/ abstract class StaticLibraryBinaryInternal extends BaseNativeBinary implements StaticLibraryBinary
	, Buildable
	, CreateTaskMixIn
	, HasObjectFilesToBinaryTask
{
	@Inject
	public StaticLibraryBinaryInternal(ModelObjectRegistry<Task> taskRegistry, ObjectFactory objects, ProviderFactory providers) {
		super(objects, providers);
		getExtensions().add("createTask", taskRegistry.register(getIdentifier().child(TaskName.of("create")), CreateStaticLibraryTask.class).asProvider());
	}

	@Override
	public TaskProvider<CreateStaticLibraryTask> getCreateOrLinkTask() {
		return getCreateTask();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.of(getCreateOrLinkTask());
	}

	@Override
	public String toString() {
		return "static library binary '" + getName() + "'";
	}
}
