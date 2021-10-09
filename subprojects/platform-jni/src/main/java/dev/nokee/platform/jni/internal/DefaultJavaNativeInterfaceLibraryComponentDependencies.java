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
package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

public class DefaultJavaNativeInterfaceLibraryComponentDependencies extends DefaultJavaNativeInterfaceNativeComponentDependencies implements JavaNativeInterfaceLibraryComponentDependencies, ComponentDependencies {
	@Getter private final DependencyBucket api;
	@Getter private final DependencyBucket jvmImplementation;
	@Getter private final DependencyBucket jvmRuntimeOnly;

	@Inject
	public DefaultJavaNativeInterfaceLibraryComponentDependencies(ComponentDependenciesInternal delegate) {
		super(delegate);
		this.api = delegate.create("api", this::configureApiConfiguration);
		this.jvmImplementation = delegate.create("jvmImplementation", this::configureImplementationConfiguration);
		this.jvmRuntimeOnly = delegate.create("jvmRuntimeOnly", this::configureRuntimeOnlyConfiguration);
	}

	private void configureApiConfiguration(Configuration configuration) {
	}

	private void configureImplementationConfiguration(Configuration configuration) {
		configuration.extendsFrom(api.getAsConfiguration());
	}

	private void configureRuntimeOnlyConfiguration(Configuration configuration) {
		configuration.extendsFrom(jvmImplementation.getAsConfiguration());
	}

	@Override
	public void jvmImplementation(Object notation) {
		jvmImplementation.addDependency(notation);
	}

	@Override
	public void jvmImplementation(Object notation, Action<? super ModuleDependency> action) {
		jvmImplementation.addDependency(notation, action);
	}

	@Override
	public void jvmRuntimeOnly(Object notation) {
		jvmRuntimeOnly.addDependency(notation);
	}

	@Override
	public void jvmRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		jvmRuntimeOnly.addDependency(notation, action);
	}
}
