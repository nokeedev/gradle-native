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
package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.plugins.ExtensionAware;

import javax.inject.Inject;

public /*final*/ abstract class DefaultNativeComponentDependencies implements NativeComponentDependencies, ExtensionAware {
	@Inject
	public DefaultNativeComponentDependencies(ModelObjectIdentifier identifier, ModelObjectRegistry<DependencyBucket> bucketRegistry) {
		getExtensions().add("compileOnly", bucketRegistry.register(identifier.child("compileOnly"), DeclarableDependencyBucketSpec.class).get());
		getExtensions().add("implementation", bucketRegistry.register(identifier.child("implementation"), DeclarableDependencyBucketSpec.class).get());
		getExtensions().add("runtimeOnly", bucketRegistry.register(identifier.child("runtimeOnly"), DeclarableDependencyBucketSpec.class).get());
		getExtensions().add("linkOnly", bucketRegistry.register(identifier.child("linkOnly"), DeclarableDependencyBucketSpec.class).get());
	}

	@Override
	public DeclarableDependencyBucketSpec getCompileOnly() {
		return (DeclarableDependencyBucketSpec) getExtensions().getByName("compileOnly");
	}

	@Override
	public DeclarableDependencyBucketSpec getImplementation() {
		return (DeclarableDependencyBucketSpec) getExtensions().getByName("implementation");
	}

	@Override
	public DeclarableDependencyBucketSpec getRuntimeOnly() {
		return (DeclarableDependencyBucketSpec) getExtensions().getByName("runtimeOnly");
	}

	@Override
	public DeclarableDependencyBucketSpec getLinkOnly() {
		return (DeclarableDependencyBucketSpec) getExtensions().getByName("linkOnly");
	}
}
