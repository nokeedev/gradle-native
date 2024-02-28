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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.GenerateVirtualFileSystemOverlaysTask;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.MergeVirtualFileSystemOverlaysTask;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMixIn;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.xcode.XCTargetReference;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

public /*final*/ abstract class XCTargetAdapterSpec extends ModelElementSupport implements Variant, ModelMixIn {
	public XCTargetAdapterSpec(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry) {
		getExtensions().add("assembleDerivedDataDirTask", taskRegistry.register(getIdentifier().child(TaskName.of("assemble", "derivedDataDir")), AssembleDerivedDataDirectoryTask.class).asProvider());

		getExtensions().add("generateRemoteSwiftPackagesTask", taskRegistry.register(getIdentifier().child(TaskName.of("generate", "remoteSwiftPackages")), GenerateSwiftPackageManifestTask.class).asProvider());

		getExtensions().add("generateVirtualSystemOverlaysTask", taskRegistry.register(getIdentifier().child(TaskName.of("generate", "virtualFileSystemOverlays")), GenerateVirtualFileSystemOverlaysTask.class).asProvider());
		getExtensions().add("mergeVirtualFileSystemOverlaysTask", taskRegistry.register(getIdentifier().child(TaskName.of("merge", "virtualFileSystemOverlays")), MergeVirtualFileSystemOverlaysTask.class).asProvider());

		getExtensions().add("isolateTargetTask", taskRegistry.register(getIdentifier().child(TaskName.of("isolate", "target")), XCTargetIsolationTask.class).asProvider());

		getExtensions().add("targetTask", taskRegistry.register(getIdentifier().child(TaskName.lifecycle()), XcodeTargetExecTask.class).asProvider());
	}

	public abstract Property<XCTargetReference> getTarget();

	public String getConfiguration() {
		return ((BuildVariantInternal) getBuildVariant()).getAxisValue(CoordinateAxis.of(XcodeBuildAdapterPlugin.XCConfiguration.class)).getName();
	}

	@NestedObject
	public abstract NamedDomainObjectProvider<ResolvableDependencyBucketSpec> getDerivedData();

	@NestedObject
	public abstract NamedDomainObjectProvider<ConsumableDependencyBucketSpec> getDerivedDataElements();

	public TaskProvider<AssembleDerivedDataDirectoryTask> getAssembleDerivedDataDirectoryTask() {
		return mixedIn("assembleDerivedDataDirTask");
	}

	@NestedObject
	public abstract NamedDomainObjectProvider<ResolvableDependencyBucketSpec> getRemoteSwiftPackages();

	@NestedObject
	public abstract NamedDomainObjectProvider<ConsumableDependencyBucketSpec> getRemoteSwiftPackagesElements();

	public TaskProvider<GenerateSwiftPackageManifestTask> getGenerateRemoteSwiftPackagesTask() {
		return mixedIn("generateRemoteSwiftPackagesTask");
	}

	@NestedObject
	public abstract NamedDomainObjectProvider<ResolvableDependencyBucketSpec> getVirtualFileSystemOverlays();

	public TaskProvider<MergeVirtualFileSystemOverlaysTask> getMergeVirtualFileSystemOverlaysTask() {
		return mixedIn("mergeVirtualFileSystemOverlaysTask");
	}

	@NestedObject
	public abstract NamedDomainObjectProvider<ConsumableDependencyBucketSpec> getVirtualFileSystemOverlaysElements();

	public TaskProvider<GenerateVirtualFileSystemOverlaysTask> getGenerateVirtualSystemOverlaysTask() {
		return mixedIn("generateVirtualSystemOverlaysTask");
	}

	public TaskProvider<XCTargetIsolationTask> getIsolateTargetTask() {
		return mixedIn("isolateTargetTask");
	}

	public TaskProvider<XcodeTargetExecTask> getTargetTask() {
		return mixedIn("targetTask");
	}

	@Override
	protected String getTypeName() {
		return "variant of XCTarget";
	}
}
