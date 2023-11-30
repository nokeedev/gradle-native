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

import dev.nokee.language.base.internal.SourceComponentSpec;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.DependentComponentSpec;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibraryEx;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;

public interface NativeLibrarySpecEx extends NativeLibraryEx
	, ExtensionAwareMixIn
	, NativeLibraryComponent
	, SourceComponentSpec
	, DependentComponentSpec<NativeLibraryComponentDependencies>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, ObjectsTaskMixIn
	, AssembleTaskMixIn
{
	@NestedObject
	DefaultNativeLibraryComponentDependencies getDependencies();
}
