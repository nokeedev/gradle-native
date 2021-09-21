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
package dev.nokee.platform.nativebase;

import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetBuildTypeFactory;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import org.gradle.api.provider.SetProperty;

/**
 * Represents a component that targets different build type.
 *
 * @since 0.5
 */
public interface TargetBuildTypeAwareComponent {
	/**
	 * Specifies the target build type this component should be built for.
	 * The {@link #getBuildTypes()} property (see {@link TargetBuildTypeFactory}) can be used to construct common build types.
	 *
	 * <p>For example:</p>
	 * <pre>
	 * targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]
	 * </pre>
	 *
	 * @return a property for configuring the {@link TargetBuildType}, never null.
	 */
	SetProperty<TargetBuildType> getTargetBuildTypes();

	/**
	 * Returns a factory to create target linkages when configuring {@link #getTargetBuildTypes()}.
	 *
	 * @return a {@link TargetBuildTypeFactory} for creating {@link TargetBuildType} instance, never null.
	 */
	default TargetBuildTypeFactory getBuildTypes() {
		return NativeRuntimeBasePlugin.TARGET_BUILD_TYPE_FACTORY;
	}
}
