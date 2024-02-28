/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.jni;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasApiDependencyBucket;

/**
 * Allows the API, JVM implementation and native implementation dependencies of a Java Native Interface (JNI) library to be specified.
 * It also allows JVM runtime only as well as native link only and runtime only dependencies of a JNI library to be specified.
 *
 * @since 0.5
 */
public interface JavaNativeInterfaceLibraryComponentDependencies extends JavaNativeInterfaceNativeComponentDependencies, ComponentDependencies, HasApiDependencyBucket {
	/**
	 * Returns the JVM runtime only bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the JVM runtime only implementation bucket of dependencies, never null.
	 */
	DeclarableDependencyBucket getJvmRuntimeOnly();

	/**
	 * Returns the JVM implementation bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the JVM implementation bucket of dependencies, never null.
	 */
	DeclarableDependencyBucket getJvmImplementation();
}
