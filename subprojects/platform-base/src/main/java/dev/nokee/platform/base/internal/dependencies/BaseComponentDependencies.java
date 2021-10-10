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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

import java.util.Optional;

/**
 * Any custom typed component dependencies should extend this type to benefit from the Groovy DSL mixed in.
 */
public class BaseComponentDependencies implements ComponentDependenciesInternal, ExtensionAware {
	private final ComponentDependenciesInternal delegate;

	protected BaseComponentDependencies(ComponentDependenciesInternal delegate) {
		this.delegate = delegate;
	}

	@Override
	public DomainObjectIdentifierInternal getOwnerIdentifier() {
		return delegate.getOwnerIdentifier();
	}

	@Override
	public DependencyBucket add(DependencyBucket bucket) {
		return delegate.add(bucket);
	}

	@Override
	public DependencyBucket create(String name, Action<Configuration> action) {
		return delegate.create(name, action);
	}

	@Override
	public DependencyBucket getByName(String name) {
		return delegate.getByName(name);
	}

	@Override
	public void add(String bucketName, Object notation) {
		delegate.add(bucketName, notation);
	}

	@Override
	public void add(String bucketName, Object notation, Action<? super ModuleDependency> action) {
		delegate.add(bucketName, notation, action);
	}

	@Override
	public void configureEach(Action<? super DependencyBucket> action) {
		delegate.configureEach(action);
	}

	@Override
	public Optional<DependencyBucket> findByName(String name) {
		return delegate.findByName(name);
	}

	@Override
	public ExtensionContainer getExtensions() {
		return ((ExtensionAware) delegate).getExtensions();
	}
}
