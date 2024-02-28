/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.base;

import org.gradle.api.artifacts.Configuration;

/**
 * Represents something that carries a compile only dependency bucket represented by a {@link Configuration}.
 * A compile only dependency is not visible to consumers that are compiled or linked against this component.
 *
 * @since 0.5
 */
public interface HasCompileOnlyDependencyBucket {
	/**
	 * Returns the compile only bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the compile only bucket of dependencies, never null.
	 */
	DeclarableDependencyBucket getCompileOnly();
}
