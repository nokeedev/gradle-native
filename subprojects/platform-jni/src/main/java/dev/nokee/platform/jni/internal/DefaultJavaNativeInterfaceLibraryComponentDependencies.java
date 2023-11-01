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
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.utils.ConfigureUtils;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.plugins.ExtensionAware;

public abstract class DefaultJavaNativeInterfaceLibraryComponentDependencies implements JavaNativeInterfaceLibraryComponentDependencies, ExtensionAware {
	public DefaultJavaNativeInterfaceLibraryComponentDependencies(ModelObjectIdentifier identifier, ModelObjectRegistry<DependencyBucket> bucketRegistry) {
		getExtensions().add("api", bucketRegistry.register(identifier.child("api"), DeclarableDependencyBucketSpec.class).get());
		 getExtensions().create("jvm", DefaultJvmComponentDependencies.class, identifier.child("jvm"), bucketRegistry);
		 getExtensions().create("native", DefaultNativeComponentDependencies.class, identifier.child("native"), bucketRegistry);
		 getExtensions().add("apiElements", bucketRegistry.register(identifier.child("apiElements"), ConsumableDependencyBucketSpec.class).get());
		 getExtensions().add("runtimeElements", bucketRegistry.register(identifier.child("runtimeElements"), ConsumableDependencyBucketSpec.class).get());
	}

	public DefaultNativeComponentDependencies getNative() {
		return (DefaultNativeComponentDependencies) getExtensions().getByName("native");
	}

	@Override
	public void api(Object notation) {
		getApi().addDependency(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		getApi().addDependency(notation, action);
	}

	@Override
	public void api(Object notation, @SuppressWarnings("rawtypes") Closure closure) {
		getApi().addDependency(notation, ConfigureUtils.configureUsing(closure));
	}

	@Override
	public DeclarableDependencyBucketSpec getApi() {
		return (DeclarableDependencyBucketSpec) getExtensions().getByName("api");
	}

	@Override
	public void jvmImplementation(Object notation) {
		getJvmImplementation().addDependency(notation);
	}

	@Override
	public void jvmImplementation(Object notation, Action<? super ModuleDependency> action) {
		getJvmImplementation().addDependency(notation, action);
	}

	@Override
	public void jvmImplementation(Object notation, @SuppressWarnings("rawtypes") Closure closure) {
		getJvmImplementation().addDependency(notation, ConfigureUtils.configureUsing(closure));
	}

	@Override
	public DeclarableDependencyBucketSpec getJvmImplementation() {
		return getExtensions().getByType(DefaultJvmComponentDependencies.class).getImplementation();
	}

	@Override
	public void jvmRuntimeOnly(Object notation) {
		getJvmRuntimeOnly().addDependency(notation);
	}

	@Override
	public void jvmRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		getJvmRuntimeOnly().addDependency(notation, action);
	}

	@Override
	public void jvmRuntimeOnly(Object notation, @SuppressWarnings("rawtypes") Closure closure) {
		getJvmRuntimeOnly().addDependency(notation, ConfigureUtils.configureUsing(closure));
	}

	@Override
	public DeclarableDependencyBucketSpec getJvmRuntimeOnly() {
		return getExtensions().getByType(DefaultJvmComponentDependencies.class).getRuntimeOnly();
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
		return getExtensions().getByType(DefaultNativeComponentDependencies.class).getImplementation();
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
		return getExtensions().getByType(DefaultNativeComponentDependencies.class).getLinkOnly();
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
		return getExtensions().getByType(DefaultNativeComponentDependencies.class).getRuntimeOnly();
	}

	public ConsumableDependencyBucketSpec getApiElements() {
		return (ConsumableDependencyBucketSpec) getExtensions().getByName("apiElements");
	}

	public ConsumableDependencyBucketSpec getRuntimeElements() {
		return (ConsumableDependencyBucketSpec) getExtensions().getByName("runtimeElements");
	}
}
