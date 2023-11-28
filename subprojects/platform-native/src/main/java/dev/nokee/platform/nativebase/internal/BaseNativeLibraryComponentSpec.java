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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.decorators.Decorate;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.NestedViewDecorator;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTask;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibraryExtension;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

public abstract class BaseNativeLibraryComponentSpec extends ModelElementSupport implements NativeLibraryExtension
	, ExtensionAwareMixIn
	, NativeComponentSpec
	, NativeLibraryComponent
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>>
	, DependencyAwareComponentMixIn<NativeLibraryComponentDependencies>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, TargetedNativeComponentSpec
	, ObjectsTaskMixIn
	, HasAssembleTask
{
	@NestedObject
	public abstract DefaultNativeLibraryComponentDependencies getDependencies();

	@NestedObject
	public abstract TaskProvider<Task> getAssembleTask();

	@NestedObject
	public abstract TaskProvider<Task> getObjectsTask();

	@Decorate(NestedViewDecorator.class)
	public abstract TaskView<Task> getTasks();

	@Decorate(NestedViewDecorator.class)
	public abstract BinaryView<Binary> getBinaries();

	@Decorate(NestedViewDecorator.class)
	public abstract SourceView<LanguageSourceSet> getSources();
}
