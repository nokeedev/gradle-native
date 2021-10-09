/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.Getter;
import org.gradle.api.artifacts.Configuration;

import javax.inject.Inject;

public class DefaultNativeComponentDependencies extends BaseComponentDependencies implements NativeComponentDependencies, ComponentDependencies {
	@Getter private final DependencyBucket implementation;
	@Getter private final DependencyBucket compileOnly;
	@Getter private final DependencyBucket linkOnly;
	@Getter private final DependencyBucket runtimeOnly;

	@Inject
	public DefaultNativeComponentDependencies(ComponentDependenciesInternal delegate) {
		super(delegate);
		this.implementation = delegate.create("implementation", this::configureImplementationConfiguration);
		this.compileOnly = delegate.create("compileOnly", this::configureCompileOnlyConfiguration);
		this.linkOnly = delegate.create("linkOnly", this::configureLinkOnlyConfiguration);
		this.runtimeOnly = delegate.create("runtimeOnly", this::configureRuntimeOnlyConfiguration);
	}

	private void configureImplementationConfiguration(Configuration configuration) {
	}

	private void configureCompileOnlyConfiguration(Configuration configuration) {
	}

	private void configureLinkOnlyConfiguration(Configuration configuration) {
	}

	private void configureRuntimeOnlyConfiguration(Configuration configuration) {
	}
}
