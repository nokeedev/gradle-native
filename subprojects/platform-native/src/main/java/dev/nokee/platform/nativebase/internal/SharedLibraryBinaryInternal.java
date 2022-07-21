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
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.linking.HasLinkTask;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkTask;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.TaskDependencyUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Buildable;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import javax.inject.Inject;

public class SharedLibraryBinaryInternal extends BaseNativeBinary implements SharedLibraryBinary
	, Buildable
	, HasPublicType
	, ModelBackedNamedMixIn
	, ModelBackedHasBaseNameMixIn
	, HasLinkTask<LinkSharedLibrary, LinkSharedLibraryTask>
	, HasObjectFilesToBinaryTask
	, HasLinkLibrariesDependencyBucket
	, HasRuntimeLibrariesDependencyBucket
{
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providerFactory;
	@Getter RegularFileProperty linkedFile;

	// TODO: The dependencies passed over here should be a read-only like only FileCollections
	@Inject
	public SharedLibraryBinaryInternal(BinaryIdentifier identifier, TargetMachine targetMachine, ObjectFactory objects, ProviderFactory providers) {
		super(identifier, targetMachine, objects, providers);
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
	public TaskProvider<LinkSharedLibrary> getLinkTask() {
		return (TaskProvider<LinkSharedLibrary>) ModelElements.of(this, NativeLinkTask.class).as(LinkSharedLibrary.class).asProvider();
	}

	@Override
	public TaskProvider<LinkSharedLibraryTask> getCreateOrLinkTask() {
		return (TaskProvider<LinkSharedLibraryTask>) ModelElements.of(this, NativeLinkTask.class).as(LinkSharedLibraryTask.class).asProvider();
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
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(SharedLibraryBinary.class);
	}

	@Override
	public String toString() {
		return "shared library binary '" + getName() + "'";
	}
}
