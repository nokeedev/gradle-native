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
package dev.nokee.platform.jni.internal.mixins;

import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

public interface NativeLinkOnlyDependencyBucketMixIn {
	default void nativeLinkOnly(Dependency dependency) {
		getNativeLinkOnly().addDependency(dependency);
	}

	default <DependencyType extends Dependency> void nativeLinkOnly(DependencyType dependency, Action<? super DependencyType> configureAction) {
		getNativeLinkOnly().addDependency(dependency, configureAction);
	}

	default <DependencyType extends Dependency> void nativeLinkOnly(Provider<DependencyType> dependencyProvider) {
		getNativeLinkOnly().addDependency(dependencyProvider);
	}

	default <DependencyType extends Dependency> void nativeLinkOnly(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
		getNativeLinkOnly().addDependency(dependencyProvider, configureAction);
	}

	default void nativeLinkOnly(FileCollection fileCollection) {
		getNativeLinkOnly().addDependency(fileCollection);
	}

	default void nativeLinkOnly(Project project) {
		getNativeLinkOnly().addDependency(project);
	}

	default void nativeLinkOnly(CharSequence dependencyNotation) {
		getNativeLinkOnly().addDependency(dependencyNotation);
	}

	default void nativeLinkOnly(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction) {
		getNativeLinkOnly().addDependency(dependencyNotation, configureAction);
	}

	DeclarableDependencyBucketSpec getNativeLinkOnly();
}
