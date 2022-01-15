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

/**
 * A factory for creating {@link TargetBuildType} instances.
 *
 * @since 0.5
 */
@Deprecated
public interface TargetBuildTypeFactory {
	/**
	 * Creates a build type of the specified name.
	 *
	 * @param name  the name of the build type to create, must not be null
	 * @return a {@link TargetBuildType} instance representing a build type with the specified name, never null.
	 */
	TargetBuildType named(String name);
}
