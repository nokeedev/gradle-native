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
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import javax.inject.Inject;

public /*final*/ abstract class NativeSharedLibraryBinarySpec extends BaseNativeBinary implements SharedLibraryBinary
	, NativeLanguageSourceSetAware
	, LinkTaskMixIn<LinkSharedLibrary, LinkSharedLibraryTask>
	, HasLinkLibrariesDependencyBucket
	, HasRuntimeLibrariesDependencyBucket
	, CompileTasksMixIn
{
	// TODO: The dependencies passed over here should be a read-only like only FileCollections
	@Inject
	public NativeSharedLibraryBinarySpec(ModelObjectRegistry<DependencyBucket> bucketRegistry, ObjectFactory objects, ProviderFactory providers) {
		super(objects, providers);
		getExtensions().add("linkLibraries", bucketRegistry.register(getIdentifier().child("linkLibraries"), ResolvableDependencyBucketSpec.class).get());
		getExtensions().add("runtimeLibraries", bucketRegistry.register(getIdentifier().child("runtimeLibraries"), ResolvableDependencyBucketSpec.class).get());
	}

	@Override
	public TaskProvider<LinkSharedLibraryTask> getCreateOrLinkTask() {
		return getLinkTask();
	}

	public abstract RegularFileProperty getLinkedFile();

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
	protected String getTypeName() {
		return "shared library binary";
	}
}