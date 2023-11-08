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
package dev.nokee.platform.jni.internal;

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.utils.ConfigureUtils;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.plugins.ExtensionAware;

import javax.inject.Inject;

public abstract class DefaultJavaNativeInterfaceNativeComponentDependencies implements JavaNativeInterfaceNativeComponentDependencies, ExtensionAware {
	@Inject
	public DefaultJavaNativeInterfaceNativeComponentDependencies(ModelObjectIdentifier identifier, ModelObjectRegistry<DependencyBucket> bucketRegistry) {
		getExtensions().create("native", DefaultNativeComponentDependencies.class, identifier.child("native"), bucketRegistry);
	}

	public DefaultNativeComponentDependencies getNative() {
		return (DefaultNativeComponentDependencies) getExtensions().getByName("native");
	}

	@Override
	public void nativeImplementation(Object notation) {
		getNativeImplementation().addDependency(notation);
	}

	@Override
	public void nativeImplementation(Object notation, Action<? super ModuleDependency> action) {
		getNativeImplementation().addDependency(notation, action);
	}

	@Override
	public void nativeImplementation(Object notation, @SuppressWarnings("rawtypes") Closure closure) {
		getNativeImplementation().addDependency(notation, ConfigureUtils.configureUsing(closure));
	}

	@Override
	public DeclarableDependencyBucketSpec getNativeImplementation() {
		return getNative().getImplementation();
	}

	@Override
	public void nativeLinkOnly(Object notation) {
		getNativeLinkOnly().addDependency(notation);
	}

	@Override
	public void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action) {
		getNativeLinkOnly().addDependency(notation, action);
	}

	@Override
	public void nativeLinkOnly(Object notation, @SuppressWarnings("rawtypes") Closure closure) {
		getNativeLinkOnly().addDependency(notation, ConfigureUtils.configureUsing(closure));
	}

	@Override
	public DeclarableDependencyBucketSpec getNativeLinkOnly() {
		return getNative().getLinkOnly();
	}

	@Override
	public void nativeRuntimeOnly(Object notation) {
		getNativeRuntimeOnly().addDependency(notation);
	}

	@Override
	public void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		getNativeRuntimeOnly().addDependency(notation, action);
	}

	@Override
	public void nativeRuntimeOnly(Object notation, @SuppressWarnings("rawtypes") Closure closure) {
		getNativeRuntimeOnly().addDependency(notation, ConfigureUtils.configureUsing(closure));
	}

	@Override
	public DeclarableDependencyBucketSpec getNativeRuntimeOnly() {
		return getNative().getRuntimeOnly();
	}
}
