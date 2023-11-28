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

package dev.nokee.platform.base.internal.mixins;

import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.HasImplementationDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

public interface ImplementationDependencyBucketMixIn extends HasImplementationDependencyBucket {
	@Override
	@NestedObject
	DeclarableDependencyBucketSpec getImplementation();

	default void implementation(Dependency dependency) {
		getImplementation().addDependency(dependency);
	}

	default <DependencyType extends Dependency> void implementation(DependencyType dependency, Action<? super DependencyType> configureAction) {
		getImplementation().addDependency(dependency, configureAction);
	}

	default <DependencyType extends Dependency> void implementation(Provider<DependencyType> dependencyProvider) {
		getImplementation().addDependency(dependencyProvider);
	}

	default <DependencyType extends Dependency> void implementation(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
		getImplementation().addDependency(dependencyProvider, configureAction);
	}

	default void implementation(FileCollection fileCollection) {
		getImplementation().addDependency(fileCollection);
	}

	default void implementation(Project project) {
		getImplementation().addDependency(project);
	}

	default void implementation(Project project, Action<? super ProjectDependency> configureAction) {
		getImplementation().addDependency(project, configureAction);
	}

	default void implementation(CharSequence dependencyNotation) {
		getImplementation().addDependency(dependencyNotation);
	}

	default void implementation(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction) {
		getImplementation().addDependency(dependencyNotation, configureAction);
	}
}
