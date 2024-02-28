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
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderConvertible;

/**
 * Represent a bucket of declarable dependencies.
 * These dependencies are neither incoming or outgoing dependencies.
 *
 * @since 0.5
 */
public interface DeclarableDependencyBucket extends DependencyBucket {
	void addDependency(CharSequence dependencyNotation);

	void addDependency(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction);

	void addDependency(Dependency dependency);

	void addDependency(FileCollection fileCollection);

	void addDependency(FileCollection fileCollection, Action<? super FileCollectionDependency> configureAction);

	<DependencyType extends Dependency> void addDependency(DependencyType dependency, Action<? super DependencyType> configureAction);

	<DependencyType extends Dependency> void addDependency(Provider<DependencyType> dependencyProvider);

	<DependencyType extends Dependency> void addDependency(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction);

	void addDependency(ProviderConvertible<? extends Dependency> dependencyProvider);

	<DependencyType extends Dependency> void addDependency(ProviderConvertible<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction);

	void addDependency(Project project);

	void addDependency(Project project, Action<? super ProjectDependency> configureAction);

	void addBundle(Iterable<? extends Dependency> bundle);

	<DependencyType extends Dependency> void addBundle(Iterable<? extends DependencyType> bundle, Action<? super DependencyType> configureAction);

	void addBundle(Provider<? extends Iterable<? extends Dependency>> bundleProvider);

	<DependencyType extends Dependency> void addBundle(Provider<? extends Iterable<? extends DependencyType>> bundleProvider, Action<? super DependencyType> configureAction);

	void addBundle(ProviderConvertible<? extends Iterable<? extends Dependency>> bundleProvider);

	<DependencyType extends Dependency> void addBundle(ProviderConvertible<? extends Iterable<? extends DependencyType>> bundleProvider, Action<? super DependencyType> configureAction);
}
