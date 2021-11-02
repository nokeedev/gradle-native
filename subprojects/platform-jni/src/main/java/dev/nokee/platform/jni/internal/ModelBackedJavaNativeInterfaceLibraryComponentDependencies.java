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

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.util.ConfigureUtil;

public final class ModelBackedJavaNativeInterfaceLibraryComponentDependencies implements JavaNativeInterfaceLibraryComponentDependencies, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

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
		getApi().addDependency(notation, ConfigureUtil.configureUsing(closure));
	}

	@Override
	public DependencyBucket getApi() {
		return ModelProperties.getProperty(this, "api").as(DependencyBucket.class).get();
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
		getJvmImplementation().addDependency(notation, ConfigureUtil.configureUsing(closure));
	}

	@Override
	public DependencyBucket getJvmImplementation() {
		return ModelProperties.getProperty(this, "jvmImplementation").as(DependencyBucket.class).get();
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
		getJvmRuntimeOnly().addDependency(notation, ConfigureUtil.configureUsing(closure));
	}

	@Override
	public DependencyBucket getJvmRuntimeOnly() {
		return ModelProperties.getProperty(this, "jvmRuntimeOnly").as(DependencyBucket.class).get();
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
		getNativeImplementation().addDependency(notation, ConfigureUtil.configureUsing(closure));
	}

	@Override
	public DependencyBucket getNativeImplementation() {
		return ModelProperties.getProperty(this, "nativeImplementation").as(DependencyBucket.class).get();
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
		getNativeLinkOnly().addDependency(notation, ConfigureUtil.configureUsing(closure));
	}

	@Override
	public DependencyBucket getNativeLinkOnly() {
		return ModelProperties.getProperty(this, "nativeLinkOnly").as(DependencyBucket.class).get();
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
		getNativeRuntimeOnly().addDependency(notation, ConfigureUtil.configureUsing(closure));
	}

	@Override
	public DependencyBucket getNativeRuntimeOnly() {
		return ModelProperties.getProperty(this, "nativeRuntimeOnly").as(DependencyBucket.class).get();
	}

	@Override
	public ModelNode getNode() {
		return node;
	}
}
