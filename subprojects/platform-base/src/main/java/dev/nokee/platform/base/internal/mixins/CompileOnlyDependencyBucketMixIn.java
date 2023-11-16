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
import dev.nokee.platform.base.HasCompileOnlyDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

public interface CompileOnlyDependencyBucketMixIn extends ModelMixIn, HasCompileOnlyDependencyBucket {
	default DeclarableDependencyBucketSpec getCompileOnly() {
		return mixedIn("compileOnly");
	}

	default void compileOnly(Dependency dependency) {
		getCompileOnly().addDependency(dependency);
	}

	default <DependencyType extends Dependency> void compileOnly(DependencyType dependency, Action<? super DependencyType> configureAction) {
		throw new UnsupportedOperationException();
//		getCompileOnly().addDependency(dependency, configureAction);
	}

	default <DependencyType extends Dependency> void compileOnly(Provider<DependencyType> dependencyProvider) {
		getCompileOnly().addDependency(dependencyProvider);
	}

	default <DependencyType extends Dependency> void compileOnly(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
		throw new UnsupportedOperationException();
//		getCompileOnly().addDependency(dependencyProvider, configureAction);
	}

	default void compileOnly(FileCollection fileCollection) {
		getCompileOnly().addDependency(fileCollection);
	}

	default void compileOnly(Project project) {
		getCompileOnly().addDependency(project);
	}

	default void compileOnly(CharSequence dependencyNotation) {
		getCompileOnly().addDependency(dependencyNotation);
	}

	default void compileOnly(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction) {
		getCompileOnly().addDependency(dependencyNotation, configureAction);
	}
}
