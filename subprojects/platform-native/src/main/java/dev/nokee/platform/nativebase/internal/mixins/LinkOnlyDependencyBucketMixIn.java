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

package dev.nokee.platform.nativebase.internal.mixins;

import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.nativebase.HasLinkOnlyDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

public interface LinkOnlyDependencyBucketMixIn extends HasLinkOnlyDependencyBucket {
	@Override
	@NestedObject
	DeclarableDependencyBucketSpec getLinkOnly();

	default void linkOnly(Dependency dependency) {
		getLinkOnly().addDependency(dependency);
	}

	default <DependencyType extends Dependency> void linkOnly(DependencyType dependency, Action<? super DependencyType> configureAction) {
		getLinkOnly().addDependency(dependency, configureAction);
	}

	default <DependencyType extends Dependency> void linkOnly(Provider<DependencyType> dependencyProvider) {
		getLinkOnly().addDependency(dependencyProvider);
	}

	default <DependencyType extends Dependency> void linkOnly(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
		getLinkOnly().addDependency(dependencyProvider, configureAction);
	}

	default void linkOnly(FileCollection fileCollection) {
		getLinkOnly().addDependency(fileCollection);
	}

	default void linkOnly(Project project) {
		getLinkOnly().addDependency(project);
	}

	default void linkOnly(Project project, Action<? super ProjectDependency> configureAction) {
		getLinkOnly().addDependency(project, configureAction);
	}

	default void linkOnly(CharSequence dependencyNotation) {
		getLinkOnly().addDependency(dependencyNotation);
	}

	default void linkOnly(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction) {
		getLinkOnly().addDependency(dependencyNotation, configureAction);
	}
}
