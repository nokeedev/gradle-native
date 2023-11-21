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

package dev.nokee.platform.nativebase.internal;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantViewFactory;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibraryExtension;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import org.gradle.api.Task;

import javax.inject.Inject;

public  /*final*/ abstract class NativeLibrarySpec extends ModelElementSupport implements NativeLibraryExtension
	, NativeLibraryComponent
	, ExtensionAwareMixIn
	, DependencyAwareComponentMixIn<NativeLibraryComponentDependencies>
	, VariantAwareComponentMixIn<NativeLibrary>
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, TargetMachineAwareComponentMixIn
	, TargetBuildTypeAwareComponentMixIn
	, TargetLinkageAwareComponentMixIn
	, AssembleTaskMixIn
	, HasDevelopmentVariant<NativeLibrary>
	, ObjectsTaskMixIn {
	@Inject
	public NativeLibrarySpec(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry, Factory<BinaryView<Binary>> binariesFactory, Factory<SourceView<LanguageSourceSet>> sourcesFactory, Factory<TaskView<Task>> tasksFactory, VariantViewFactory variantsFactory, Factory<DefaultVariantDimensions> dimensionsFactory) {
		getExtensions().create("dependencies", DefaultNativeLibraryComponentDependencies.class, getIdentifier(), bucketRegistry);
		getExtensions().add("assembleTask", taskRegistry.register(getIdentifier().child(TaskName.of("assemble")), Task.class).asProvider());
		getExtensions().add("binaries", binariesFactory.create());
		getExtensions().add("sources", sourcesFactory.create());
		getExtensions().add("tasks", tasksFactory.create());
		getExtensions().add("objectsTask", taskRegistry.register(getIdentifier().child(TaskName.of("objects")), Task.class).asProvider());
		getExtensions().add("variants", variantsFactory.create(NativeLibrary.class));
		getExtensions().add("dimensions", dimensionsFactory.create());
	}

	@Override
	public DefaultNativeLibraryComponentDependencies getDependencies() {
		return (DefaultNativeLibraryComponentDependencies) DependencyAwareComponentMixIn.super.getDependencies();
	}

	@Override
	protected String getTypeName() {
		return "native library";
	}
}
