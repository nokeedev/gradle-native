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
package dev.nokee.model;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

/**
 * A factory to create {@link Dependency} instance.
 *
 * @since 0.5
 */
public interface DependencyFactory {
	/**
	 * Creates a dependency factory for the specified project.
	 *
	 * @param project  the project to use as dependency factory, must not be null
	 * @return a {@link Dependency} factory, never null
	 */
	static DependencyFactory forProject(Project project) {
		return new DefaultDependencyFactory(project.getDependencies());
	}

	/**
	 * Creates a dependency factory for the specified handler.
	 *
	 * @param dependencies  the handler to use as dependency factory, must not be null
	 * @return a {@link Dependency} factory, never null
	 */
	static DependencyFactory forHandler(DependencyHandler dependencies) {
		return new DefaultDependencyFactory(dependencies);
	}

	/**
	 * Creates a dependency.
	 *
	 * @param notation  the dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}
	 * @return the new dependency, never null
	 */
	Dependency create(Object notation);
}
