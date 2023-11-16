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

import dev.nokee.utils.ConfigureUtils;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;

/**
 * Represents something that carries a compile only dependency bucket represented by a {@link Configuration}.
 *
 * @since 0.5
 */
public interface HasCompileOnlyDependencyBucket {
	/**
	 * Adds a compile only dependency to this component.
	 * An compile only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	default void compileOnly(Object notation) {
		getCompileOnly().addDependency(notation);
	}

	/**
	 * Adds a compile only dependency to this component.
	 * An compile only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void compileOnly(Object notation, Action<? super ModuleDependency> action) {
		getCompileOnly().addDependency(notation, action);
	}
	default void compileOnly(Object notation, @ClosureParams(value = SimpleType.class, options = "org.gradle.api.artifacts.ModuleDependency") @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		compileOnly(notation, ConfigureUtils.configureUsing(closure));
	}

	/**
	 * Returns the compile only bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the compile only bucket of dependencies, never null.
	 */
	DeclarableDependencyBucket getCompileOnly();
}
