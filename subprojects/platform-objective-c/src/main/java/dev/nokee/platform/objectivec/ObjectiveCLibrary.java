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
package dev.nokee.platform.objectivec;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.nativebase.HasPrivateHeaders;
import dev.nokee.language.nativebase.HasPublicHeaders;
import dev.nokee.language.objectivec.HasObjectiveCSources;
import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.*;

/**
 * Configuration for a library written in Objective-C, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Objective-C Library Plugin.</p>
 *
 * @since 0.5
 */
@SuppressWarnings("deprecation")
public interface ObjectiveCLibrary extends ObjectiveCLibraryExtension, Component
	, DependencyAwareComponent<NativeLibraryComponentDependencies>
	, VariantAwareComponent<NativeLibrary>, HasDevelopmentVariant<NativeLibrary>
	, SourceAwareComponent<SourceView<LanguageSourceSet>>
	, BinaryAwareComponent
	, TaskAwareComponent
	, TargetMachineAwareComponent
	, TargetLinkageAwareComponent
	, TargetBuildTypeAwareComponent
	, HasPrivateHeaders
	, HasPublicHeaders
	, HasObjectiveCSources
	, HasBaseName
{
}
