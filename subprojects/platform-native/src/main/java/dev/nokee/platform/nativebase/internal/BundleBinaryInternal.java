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
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.linking.HasLinkTask;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkTask;
import dev.nokee.platform.nativebase.tasks.LinkBundle;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
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

public class BundleBinaryInternal extends BaseNativeBinary implements BundleBinary
	, Buildable
	, HasPublicType
	, ModelBackedNamedMixIn
	, ModelBackedHasBaseNameMixIn
	, HasLinkTask<LinkBundle, LinkBundleTask>
	, HasObjectFilesToBinaryTask
	, HasLinkLibrariesDependencyBucket
	, HasRuntimeLibrariesDependencyBucket
{
	@Inject
	public BundleBinaryInternal(ObjectFactory objects, ProviderFactory providers) {
		super(objects, providers);

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
	public TaskProvider<LinkBundle> getLinkTask() {
		return (TaskProvider<LinkBundle>) ModelElements.of(this, NativeLinkTask.class).as(LinkBundle.class).asProvider();
	}

	@Override
	public TaskProvider<LinkBundleTask> getCreateOrLinkTask() {
		return (TaskProvider<LinkBundleTask>) ModelElements.of(this, NativeLinkTask.class).as(LinkBundleTask.class).asProvider();
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
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(BundleBinary.class);
	}

	@Override
	public String toString() {
		return "bundle binary '" + getName() + "'";
	}
}
