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
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.linking.HasLinkTaskMixIn;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkTask;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.Buildable;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import javax.inject.Inject;

@DomainObjectEntities.Tag({IsBinary.class, ConfigurableTag.class, NativeLanguageSourceSetAwareTag.class})
public /*final*/ abstract class ExecutableBinaryInternal extends BaseNativeBinary implements ExecutableBinary
	, Buildable
	, HasPublicType
	, ModelBackedHasBaseNameMixIn
	, HasLinkTaskMixIn<LinkExecutable>
	, HasObjectFilesToBinaryTask
	, HasLinkLibrariesDependencyBucket
	, HasRuntimeLibrariesDependencyBucket
{
	@Inject
	public ExecutableBinaryInternal(ObjectFactory objects, ProviderFactory providers) {
		super(objects, providers);

		getCreateOrLinkTask().configure(this::configureExecutableTask);
	}

	private void configureExecutableTask(LinkExecutableTask task) {
		// Until we model the build type
		task.getDebuggable().set(false);
	}

	@Override
	public TaskProvider<LinkExecutable> getLinkTask() {
		return (TaskProvider<LinkExecutable>) ModelElements.of(this, NativeLinkTask.class).as(LinkExecutable.class).asProvider();
	}

	@Override
	public TaskProvider<LinkExecutableTask> getCreateOrLinkTask() {
		return (TaskProvider<LinkExecutableTask>) ModelElements.of(this, NativeLinkTask.class).as(LinkExecutableTask.class).asProvider();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.of(getCreateOrLinkTask());
	}

	@Override
	public boolean isBuildable() {
		try {
			return super.isBuildable() && isBuildable(getCreateOrLinkTask().get());
		} catch (Throwable ex) { // because toolchain selection calls xcrun for macOS which doesn't exists on non-mac system
			return false;
		}
	}

	private static boolean isBuildable(LinkExecutable linkTask) {
		AbstractLinkTask linkTaskInternal = (AbstractLinkTask)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(ExecutableBinary.class);
	}

	@Override
	public String toString() {
		return "executable binary '" + getName() + "'";
	}
}
