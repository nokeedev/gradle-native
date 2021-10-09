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
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import lombok.Getter;
import org.gradle.api.artifacts.Configuration;

import javax.inject.Inject;

public class DefaultNativeLibraryComponentDependencies extends DefaultNativeComponentDependencies implements NativeLibraryComponentDependencies, ComponentDependencies {
	@Getter private final DependencyBucket api;

	@Inject
	public DefaultNativeLibraryComponentDependencies(ComponentDependenciesInternal delegate) {
		super(delegate);
		this.api = delegate.create("api", this::configureApiConfiguration);
	}

	private void configureApiConfiguration(Configuration configuration) {
		// Configure this here to simplify testing, it ends up being the same
		getImplementation().getAsConfiguration().extendsFrom(configuration);
	}
}
