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
import dev.nokee.model.internal.decorators.ModelMixInSupport;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.mixins.ApiDependencyBucketMixIn;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import org.gradle.api.plugins.ExtensionAware;

import javax.inject.Inject;

public abstract class DefaultJavaNativeInterfaceLibraryComponentDependencies extends ModelMixInSupport implements JavaNativeInterfaceLibraryComponentDependencies
	, ApiDependencyBucketMixIn
	, NativeImplementationDependencyBucketMixIn
	, NativeLinkOnlyDependencyBucketMixIn
	, NativeRuntimeOnlyDependencyBucketMixIn
	, JvmImplementationDependencyBucketMixIn
	, JvmRuntimeOnlyDependencyBucketMixIn
	, ExtensionAware
{
	@Inject
	public DefaultJavaNativeInterfaceLibraryComponentDependencies(ModelObjectIdentifier identifier, ModelObjectRegistry<DependencyBucket> bucketRegistry) {
		 getExtensions().add("apiElements", bucketRegistry.register(identifier.child("apiElements"), ConsumableDependencyBucketSpec.class).get());
		 getExtensions().add("runtimeElements", bucketRegistry.register(identifier.child("runtimeElements"), ConsumableDependencyBucketSpec.class).get());
	}

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

	public ConsumableDependencyBucketSpec getApiElements() {
		return (ConsumableDependencyBucketSpec) getExtensions().getByName("apiElements");
	}

	public ConsumableDependencyBucketSpec getRuntimeElements() {
		return (ConsumableDependencyBucketSpec) getExtensions().getByName("runtimeElements");
	}
}
