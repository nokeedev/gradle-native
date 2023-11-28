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
import dev.nokee.language.objectivec.HasObjectiveCSources;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.TaskAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;

/**
 * Configuration for an application written in Objective-C, defining the dependencies that make up the application plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Objective-C Application Plugin.</p>
 *
 * @since 0.5
 */
public interface ObjectiveCApplication extends Component
	, DependencyAwareComponent<NativeApplicationComponentDependencies>
	, VariantAwareComponent<NativeApplication>, HasDevelopmentVariant<NativeApplication>
	, SourceAwareComponent<SourceView<LanguageSourceSet>>
	, BinaryAwareComponent
	, TaskAwareComponent
	, TargetMachineAwareComponent
	, TargetBuildTypeAwareComponent
	, HasPrivateHeaders
	, HasObjectiveCSources
	, HasBaseName
{
}
