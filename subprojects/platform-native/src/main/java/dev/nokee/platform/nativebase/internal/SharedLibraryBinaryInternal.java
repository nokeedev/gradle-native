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

import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAware;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.linking.LinkTaskMixIn;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.utils.TaskDependencyUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Buildable;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import javax.inject.Inject;

public /*final*/ abstract class SharedLibraryBinaryInternal extends BaseNativeBinary implements SharedLibraryBinary
	, Buildable
	, NativeLanguageSourceSetAware
	, LinkTaskMixIn<LinkSharedLibrary, LinkSharedLibraryTask>
	, HasObjectFilesToBinaryTask
	, HasLinkLibrariesDependencyBucket
	, HasRuntimeLibrariesDependencyBucket
	, CompileTasksMixIn
{
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providerFactory;
	@Getter RegularFileProperty linkedFile;

	// TODO: The dependencies passed over here should be a read-only like only FileCollections
	@Inject
	public SharedLibraryBinaryInternal(ModelObjectRegistry<DependencyBucket> bucketRegistry, ObjectFactory objects, ProviderFactory providers) {
		super(objects, providers);
		getExtensions().add("linkLibraries", bucketRegistry.register(getIdentifier().child("linkLibraries"), ResolvableDependencyBucketSpec.class).get());
		getExtensions().add("runtimeLibraries", bucketRegistry.register(getIdentifier().child("runtimeLibraries"), ResolvableDependencyBucketSpec.class).get());
		this.objects = objects;
		this.providerFactory = providers;
		this.linkedFile = objects.fileProperty();
		getCreateOrLinkTask().configure(this::configureSharedLibraryTask);

		getLinkedFile().set(getCreateOrLinkTask().flatMap(AbstractLinkTask::getLinkedFile));
		getLinkedFile().disallowChanges();
	}

	private void configureSharedLibraryTask(LinkSharedLibraryTask task) {
		// Until we model the build type
		task.getDebuggable().set(false);

		Provider<String> installName = task.getLinkedFile().getLocationOnly().map(linkedFile -> linkedFile.getAsFile().getName());
		task.getInstallName().set(installName);
	}

	@Override
	public TaskProvider<LinkSharedLibraryTask> getCreateOrLinkTask() {
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

	private static boolean isBuildable(LinkSharedLibrary linkTask) {
		AbstractLinkTask linkTaskInternal = (AbstractLinkTask)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.of(getCreateOrLinkTask());
	}

	@Override
	protected String getTypeName() {
		return "shared library binary";
	}
}
