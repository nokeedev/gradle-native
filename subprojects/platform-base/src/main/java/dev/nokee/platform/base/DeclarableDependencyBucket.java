/*
 * Copyright 2023 the original author or authors.
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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

/**
 * Represent a bucket of declarable dependencies.
 * These dependencies are neither incoming or outgoing dependencies.
 *
 * @since 0.5
 */
public interface DeclarableDependencyBucket extends DependencyBucket {
	void addDependency(Dependency dependency);

	<DependencyType extends Dependency> void addDependency(DependencyType dependency, Action<? super DependencyType> configureAction);

	<DependencyType extends Dependency> void addDependency(Provider<DependencyType> dependencyProvider);

	<DependencyType extends Dependency> void addDependency(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction);

	void addDependency(FileCollection fileCollection);

	void addDependency(Project project);

	void addDependency(CharSequence dependencyNotation);

	void addDependency(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction);
}
