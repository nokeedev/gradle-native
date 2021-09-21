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
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

public class DefaultJavaNativeInterfaceNativeComponentDependencies extends BaseComponentDependencies implements JavaNativeInterfaceNativeComponentDependencies, ComponentDependencies {
	@Getter private final DependencyBucket nativeImplementation;
	@Getter private final DependencyBucket nativeLinkOnly;
	@Getter private final DependencyBucket nativeRuntimeOnly;

	@Inject
	public DefaultJavaNativeInterfaceNativeComponentDependencies(ComponentDependenciesInternal delegate) {
		super(delegate);
		this.nativeImplementation = delegate.create("nativeImplementation", this::configureImplementationConfiguration);
		this.nativeLinkOnly = delegate.create("nativeLinkOnly", this::configureLinkOnlyConfiguration);
		this.nativeRuntimeOnly = delegate.create("nativeRuntimeOnly", this::configureRuntimeOnlyConfiguration);
	}

//	@Override
//	public DependencyBucket create(String name) {
//		return super.create("native" + StringUtils.capitalize(name));
//	}
//
//	@Override
//	public DependencyBucket create(String name, Action<Configuration> action) {
//		return super.create("native" + StringUtils.capitalize(name), action);
//	}

	private void configureImplementationConfiguration(Configuration configuration) {
	}

	private void configureLinkOnlyConfiguration(Configuration configuration) {
		configuration.extendsFrom(nativeImplementation.getAsConfiguration());
	}

	private void configureRuntimeOnlyConfiguration(Configuration configuration) {
		configuration.extendsFrom(nativeImplementation.getAsConfiguration());
	}

	@Override
	public void nativeImplementation(Object notation) {
		nativeImplementation.addDependency(notation);
	}

	@Override
	public void nativeImplementation(Object notation, Action<? super ModuleDependency> action) {
		nativeImplementation.addDependency(notation, action);
	}

	@Override
	public void nativeLinkOnly(Object notation) {
		nativeLinkOnly.addDependency(notation);
	}

	@Override
	public void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action) {
		nativeLinkOnly.addDependency(notation, action);
	}

	@Override
	public void nativeRuntimeOnly(Object notation) {
		nativeRuntimeOnly.addDependency(notation);
	}

	@Override
	public void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		nativeRuntimeOnly.addDependency(notation, action);
	}
}
