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
package dev.nokee.platform.objectivecpp;

import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;

/**
 * Configuration for a library written in Objective-C++, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Objective-C++ Library Plugin.</p>
 *
 * @since 0.5
 */
public interface ObjectiveCppLibrary extends ObjectiveCppLibraryExtension, DependencyAwareComponent<NativeLibraryComponentDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<ObjectiveCppLibrarySources>, HasPrivateHeaders, HasPublicHeaders, HasObjectiveCppSources, BaseNameAwareComponent {
	/**
	 * {@inheritDoc}
	 */
	default NativeLibraryComponentDependencies getDependencies() {
		return ModelNodeUtils.get(ModelNodes.of(this), DefaultNativeLibraryComponent.class).getDependencies();
	}
}
