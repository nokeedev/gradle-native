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
package dev.nokee.platform.nativebase;

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
 * Allows the implementation dependencies of a native component to be specified.
 *
 * @since 0.4
 */
public interface NativeComponentDependencies extends ComponentDependencies {
	/**
	 * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void implementation(Object notation);

	/**
	 * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void implementation(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void implementation(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		implementation(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Adds an native compile only dependency to this component.
	 * An compile only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void compileOnly(Object notation);

	/**
	 * Adds an native compile only dependency to this component.
	 * An compile only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void compileOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an native compile only dependency to this component.
	 * An compile only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void compileOnly(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		compileOnly(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void linkOnly(Object notation);

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void linkOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void linkOnly(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		linkOnly(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is not visible to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void runtimeOnly(Object notation);

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is visible only to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	void runtimeOnly(Object notation, Action<? super ModuleDependency> action);

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is visible only to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param closure The closure to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void runtimeOnly(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		runtimeOnly(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Returns the implementation bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the implementation bucket of dependencies, never null.
	 */
	DependencyBucket getImplementation();

	/**
	 * Returns the runtime only bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the runtime only bucket of dependencies, never null.
	 */
	DependencyBucket getRuntimeOnly();

	/**
	 * Returns the compile only bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the compile only bucket of dependencies, never null.
	 */
	DependencyBucket getCompileOnly();

	/**
	 * Returns the link only bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the link only bucket of dependencies, never null.
	 */
	DependencyBucket getLinkOnly();
}
