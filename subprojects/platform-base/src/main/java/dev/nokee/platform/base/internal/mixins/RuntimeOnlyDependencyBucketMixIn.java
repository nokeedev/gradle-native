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

import dev.nokee.model.internal.ModelMixIn;
import dev.nokee.platform.base.HasRuntimeOnlyDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

public interface RuntimeOnlyDependencyBucketMixIn extends ModelMixIn, HasRuntimeOnlyDependencyBucket {
	default DeclarableDependencyBucketSpec getRuntimeOnly() {
		return mixedIn("runtimeOnly");
	}

	default void runtimeOnly(Dependency dependency) {
		getRuntimeOnly().addDependency(dependency);
	}

	default <DependencyType extends Dependency> void runtimeOnly(DependencyType dependency, Action<? super DependencyType> configureAction) {
		getRuntimeOnly().addDependency(dependency, configureAction);
	}

	default <DependencyType extends Dependency> void runtimeOnly(Provider<DependencyType> dependencyProvider) {
		getRuntimeOnly().addDependency(dependencyProvider);
	}

	default <DependencyType extends Dependency> void runtimeOnly(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
		getRuntimeOnly().addDependency(dependencyProvider, configureAction);
	}

	default void runtimeOnly(FileCollection fileCollection) {
		getRuntimeOnly().addDependency(fileCollection);
	}

	default void runtimeOnly(Project project) {
		getRuntimeOnly().addDependency(project);
	}

	default void runtimeOnly(CharSequence dependencyNotation) {
		getRuntimeOnly().addDependency(dependencyNotation);
	}

	default void runtimeOnly(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction) {
		getRuntimeOnly().addDependency(dependencyNotation, configureAction);
	}
}
