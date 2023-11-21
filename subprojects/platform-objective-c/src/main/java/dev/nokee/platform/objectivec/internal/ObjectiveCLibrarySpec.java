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

package dev.nokee.platform.objectivec.internal;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.nativebase.internal.PrivateHeadersMixIn;
import dev.nokee.language.nativebase.internal.PublicHeadersMixIn;
import dev.nokee.language.objectivec.internal.ObjectiveCSourcesMixIn;
import dev.nokee.language.objectivec.internal.plugins.SupportObjectiveCSourceSetTag;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.ObjectsTaskMixIn;
import dev.nokee.platform.nativebase.internal.TargetBuildTypeAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.TargetLinkageAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.TargetMachineAwareComponentMixIn;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.objectivec.ObjectiveCLibrary;
import org.gradle.api.Task;

import javax.inject.Inject;

public  /*final*/ abstract class ObjectiveCLibrarySpec extends ModelElementSupport implements ObjectiveCLibrary
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
	, ObjectiveCSourcesMixIn
	, PrivateHeadersMixIn
	, PublicHeadersMixIn
	, ObjectsTaskMixIn {
	@Inject
	public ObjectiveCLibrarySpec(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry, Factory<SourceView<LanguageSourceSet>> sourcesFactory) {
		getExtensions().create("dependencies", DefaultNativeLibraryComponentDependencies.class, getIdentifier(), bucketRegistry);
		getExtensions().add("assembleTask", taskRegistry.register(getIdentifier().child(TaskName.of("assemble")), Task.class).asProvider());
		getExtensions().add("sources", sourcesFactory.create());
		getExtensions().add("objectsTask", taskRegistry.register(getIdentifier().child(TaskName.of("objects")), Task.class).asProvider());
		getExtensions().create("$objectiveCSupport", SupportObjectiveCSourceSetTag.class);
	}

	@Override
	public DefaultNativeLibraryComponentDependencies getDependencies() {
		return (DefaultNativeLibraryComponentDependencies) DependencyAwareComponentMixIn.super.getDependencies();
	}

	@Override
	protected String getTypeName() {
		return "Objective-C library";
	}
}
