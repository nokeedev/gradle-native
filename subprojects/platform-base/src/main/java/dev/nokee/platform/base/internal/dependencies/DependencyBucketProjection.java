/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import javax.inject.Inject;

public class DependencyBucketProjection {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();
	private final String name;
	private final DependencyHandler dependencyHandler;

	@Inject
	public DependencyBucketProjection(String name, DependencyHandler dependencyHandler) {
		this.name = name;
		this.dependencyHandler = dependencyHandler;
	}

	public String getName() {
		return name;
	}

	public void addDependency(Object notation) {
		Dependency dependency = dependencyHandler.create(notation);
		getAsConfiguration().getDependencies().add(dependency);
	}

	public void addDependency(Object notation, Action<? super ModuleDependency> action) {
		Dependency dependency = dependencyHandler.create(notation);
		action.execute((ModuleDependency) dependency);
		getAsConfiguration().getDependencies().add(dependency);
	}

	private Configuration getAsConfiguration() {
		return node.get(Configuration.class);
	}
}
