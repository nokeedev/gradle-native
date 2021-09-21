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
import dev.nokee.platform.base.DependencyBucket;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.util.ConfigureUtil;

/**
 * Allows the native implementation dependencies of a Java Native Interface (JNI) library to be specified.
 * It also allows native link only and runtime only dependencies of a JNI library to be specified.
 *
 * @since 0.5
 */
public interface JavaNativeInterfaceNativeComponentDependencies extends ComponentDependencies, JniLibraryNativeDependencies {
	/**
	 * Adds an native implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void nativeImplementation(Object notation);

	/**
	 * Adds an native implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void nativeImplementation(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an native implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void nativeImplementation(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		nativeImplementation(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void nativeLinkOnly(Object notation);

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 * @since 0.4
	 */
	void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 * @since 0.4
	 */
	default void nativeLinkOnly(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		nativeLinkOnly(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is not visible to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void nativeRuntimeOnly(Object notation);

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is visible only to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is visible only to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void nativeRuntimeOnly(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		nativeRuntimeOnly(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Returns the native implementation bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the native implementation bucket of dependencies, never null.
	 */
	DependencyBucket getNativeImplementation();

	/**
	 * Returns the native runtime only bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the native runtime only implementation bucket of dependencies, never null.
	 */
	DependencyBucket getNativeRuntimeOnly();

	/**
	 * Returns the native link only bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the native link only implementation bucket of dependencies, never null.
	 */
	DependencyBucket getNativeLinkOnly();
}
