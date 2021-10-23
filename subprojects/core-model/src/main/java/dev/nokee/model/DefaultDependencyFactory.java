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

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.Objects;

final class DefaultDependencyFactory implements DependencyFactory {
	private final DependencyHandler dependencies;

	DefaultDependencyFactory(DependencyHandler dependencies) {
		this.dependencies = Objects.requireNonNull(dependencies);
	}

	@Override
	public Dependency create(Object notation) {
		Objects.requireNonNull(notation);
		return dependencies.create(notation);
	}
}
