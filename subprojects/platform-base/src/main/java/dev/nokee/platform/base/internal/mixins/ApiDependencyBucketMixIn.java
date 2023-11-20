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
import dev.nokee.platform.base.HasApiDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

public interface ApiDependencyBucketMixIn extends ModelMixIn, HasApiDependencyBucket {
	default DeclarableDependencyBucketSpec getApi() {
		return mixedIn("api");
	}

	default void api(Dependency dependency) {
		getApi().addDependency(dependency);
	}

	default <DependencyType extends Dependency> void api(DependencyType dependency, Action<? super DependencyType> configureAction) {
		getApi().addDependency(dependency, configureAction);
	}

	default <DependencyType extends Dependency> void api(Provider<DependencyType> dependencyProvider) {
		getApi().addDependency(dependencyProvider);
	}

	default <DependencyType extends Dependency> void api(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
		getApi().addDependency(dependencyProvider, configureAction);
	}

	default void api(FileCollection fileCollection) {
		getApi().addDependency(fileCollection);
	}

	default void api(Project project) {
		getApi().addDependency(project);
	}

	default void api(Project project, Action<? super ProjectDependency> configureAction) {
		getApi().addDependency(project, configureAction);
	}

	default void api(CharSequence dependencyNotation) {
		getApi().addDependency(dependencyNotation);
	}

	default void api(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction) {
		getApi().addDependency(dependencyNotation, configureAction);
	}
}
