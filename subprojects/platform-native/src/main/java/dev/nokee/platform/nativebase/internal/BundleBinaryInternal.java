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
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAware;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.linking.LinkTaskMixIn;
import dev.nokee.platform.nativebase.tasks.LinkBundle;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import javax.inject.Inject;

@DomainObjectEntities.Tag({IsBinary.class, ConfigurableTag.class})
public /*final*/ abstract class BundleBinaryInternal extends BaseNativeBinary implements BundleBinary
	, Buildable
	, NativeLanguageSourceSetAware
	, LinkTaskMixIn<LinkBundle, LinkBundleTask>
	, HasObjectFilesToBinaryTask
	, HasLinkLibrariesDependencyBucket
	, HasRuntimeLibrariesDependencyBucket
	, CompileTasksMixIn
{
	@Inject
	public BundleBinaryInternal(ModelObjectRegistry<Task> taskRegistry, ModelObjectRegistry<DependencyBucket> bucketRegistry, Factory<TaskView<SourceCompile>> compileTasksFactory, ObjectFactory objects, ProviderFactory providers) {
		super(objects, providers);
		getExtensions().add("linkTask", taskRegistry.register(getIdentifier().child(TaskName.of("link")), LinkBundleTask.class).asProvider());
		getExtensions().add("linkLibraries", bucketRegistry.register(getIdentifier().child("linkLibraries"), ResolvableDependencyBucketSpec.class).get());
		getExtensions().add("runtimeLibraries", bucketRegistry.register(getIdentifier().child("runtimeLibraries"), ResolvableDependencyBucketSpec.class).get());
		getExtensions().add("compileTasks", compileTasksFactory.create());

		getCreateOrLinkTask().configure(this::configureBundleTask);
	}

	private void configureBundleTask(LinkBundleTask task) {
		// Until we model the build type
		task.getDebuggable().set(false);
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.of(getCreateOrLinkTask());
	}

	@Override
	public TaskProvider<LinkBundleTask> getCreateOrLinkTask() {
		return getLinkTask();
	}

	@Override
	public boolean isBuildable() {
		try {
			return super.isBuildable() && isBuildable(getCreateOrLinkTask().get());
		} catch (Throwable ex) { // because toolchain selection calls xcrun for macOS which doesn't exists on non-mac system
			return false;
		}
	}

	private static boolean isBuildable(LinkBundle linkTask) {
		AbstractLinkTask linkTaskInternal = (AbstractLinkTask)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	@Override
	public String toString() {
		return "bundle binary '" + getName() + "'";
	}
}
