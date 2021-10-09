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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.util.ConfigureUtil;

/**
 * Represents something that carries an implementation dependency bucket represented by a {@link Configuration}.
 *
 * @since 0.5
 */
public interface HasImplementationDependencyBucket {
	/**
	 * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	default void implementation(Object notation) {
		getImplementation().addDependency(notation);
	}

	/**
	 * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency (project dependencies are {@link ProjectDependency} and external dependencies are {@link ExternalModuleDependency}).
	 */
	default void implementation(Object notation, Action<? super ModuleDependency> action) {
		getImplementation().addDependency(notation, action);
	}

	/** @see #implementation(Object, Action) */
	default void implementation(Object notation, @ClosureParams(value = SimpleType.class, options = "org.gradle.api.artifacts.ModuleDependency") @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		implementation(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Returns the implementation bucket of dependencies for this component.
	 *
	 * @return a {@link DependencyBucket} representing the implementation bucket of dependencies, never null.
	 */
	DependencyBucket getImplementation();
}
