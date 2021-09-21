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
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;

/**
 * Allows the API and JVM implementation and native implementation dependencies of a JNI library to be specified.
 * It also allows JVM runtime only as well as native link only and runtime only dependencies of a JNI library to be specified.
 *
 * @since 0.1
 * @deprecated Use {@link JavaNativeInterfaceLibraryComponentDependencies} instead.
 */
@Deprecated
public interface JniLibraryDependencies extends JniLibraryNativeDependencies, ComponentDependencies {
	/**
	 * Adds an JVM API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void api(Object notation);

	/**
	 * Adds an JVM API dependency to this library. An API dependency is made visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void api(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an JVM implementation dependency to this library. An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void jvmImplementation(Object notation);

	/**
	 * Adds an JVM implementation dependency to this library.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void jvmImplementation(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an JVM runtime only dependency to this library.
	 * An implementation dependency is only visible to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void jvmRuntimeOnly(Object notation);

	/**
	 * Adds an JVM runtime only dependency to this library.
	 * An implementation dependency is only visible to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void jvmRuntimeOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeImplementation(Object notation);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeImplementation(Object notation, Action<? super ModuleDependency> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeLinkOnly(Object notation);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeRuntimeOnly(Object notation);

	/**
	 * {@inheritDoc}
	 */
	@Override
	void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action);
}
