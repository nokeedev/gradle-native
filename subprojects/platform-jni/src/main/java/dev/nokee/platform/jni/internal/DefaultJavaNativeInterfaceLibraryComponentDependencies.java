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

import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.mixins.ApiDependencyBucketMixIn;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.internal.mixins.JvmImplementationDependencyBucketMixIn;
import dev.nokee.platform.jni.internal.mixins.JvmRuntimeOnlyDependencyBucketMixIn;
import dev.nokee.platform.jni.internal.mixins.NativeImplementationDependencyBucketMixIn;
import dev.nokee.platform.jni.internal.mixins.NativeLinkOnlyDependencyBucketMixIn;
import dev.nokee.platform.jni.internal.mixins.NativeRuntimeOnlyDependencyBucketMixIn;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import org.gradle.api.plugins.ExtensionAware;

public abstract class DefaultJavaNativeInterfaceLibraryComponentDependencies implements JavaNativeInterfaceLibraryComponentDependencies
	, ApiDependencyBucketMixIn
	, NativeImplementationDependencyBucketMixIn
	, NativeLinkOnlyDependencyBucketMixIn
	, NativeRuntimeOnlyDependencyBucketMixIn
	, JvmImplementationDependencyBucketMixIn
	, JvmRuntimeOnlyDependencyBucketMixIn
	, ExtensionAware
{
	@NestedObject("native")
	public abstract DefaultNativeComponentDependencies getNative();

	@NestedObject("jvm")
	public abstract DefaultJvmComponentDependencies getJvm();

	@Override
	public DeclarableDependencyBucketSpec getJvmImplementation() {
		return getJvm().getImplementation();
	}

	@Override
	public DeclarableDependencyBucketSpec getJvmRuntimeOnly() {
		return getJvm().getRuntimeOnly();
	}

	@Override
	public DeclarableDependencyBucketSpec getNativeImplementation() {
		return getNative().getImplementation();
	}

	@Override
	public DeclarableDependencyBucketSpec getNativeLinkOnly() {
		return getNative().getLinkOnly();
	}

	@Override
	public DeclarableDependencyBucketSpec getNativeRuntimeOnly() {
		return getNative().getRuntimeOnly();
	}
}
