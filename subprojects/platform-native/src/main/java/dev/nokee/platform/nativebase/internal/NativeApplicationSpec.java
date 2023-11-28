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
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.mixins.BinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.TaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.NativeApplicationExtension;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;

import javax.inject.Inject;

public abstract class NativeApplicationSpec extends ModelElementSupport implements NativeApplicationExtension
	, NativeComponentSpec
	, NativeApplicationComponent
	, ExtensionAwareMixIn
	, DependencyAwareComponentMixIn<NativeApplicationComponentDependencies>
	, VariantAwareComponentMixIn<NativeApplication>
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
	, BinaryAwareComponentMixIn
	, TaskAwareComponentMixIn
	, HasDevelopmentVariant<NativeApplication>
	, AssembleTaskMixIn
	, ObjectsTaskMixIn {
	@Inject
	public NativeApplicationSpec(Factory<SourceView<LanguageSourceSet>> sourcesFactory) {
		getExtensions().add("sources", sourcesFactory.create());
	}

	@Override
	@NestedObject
	public abstract DefaultNativeApplicationComponentDependencies getDependencies();

	@Override
	protected String getTypeName() {
		return "native application";
	}
}
