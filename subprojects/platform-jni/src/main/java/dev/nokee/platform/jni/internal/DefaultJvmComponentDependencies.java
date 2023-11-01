/*
 * Copyright 2023 the original author or authors.
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
import dev.nokee.platform.base.HasImplementationDependencyBucket;
import dev.nokee.platform.base.HasRuntimeOnlyDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import org.gradle.api.plugins.ExtensionAware;

import javax.inject.Inject;

public abstract class DefaultJvmComponentDependencies implements HasImplementationDependencyBucket, HasRuntimeOnlyDependencyBucket, ExtensionAware {
	@Inject
	public DefaultJvmComponentDependencies(ModelObjectIdentifier identifier, ModelObjectRegistry<DependencyBucket> bucketRegistry) {
		getExtensions().add("implementation", bucketRegistry.register(identifier.child("implementation"), DeclarableDependencyBucketSpec.class).get());
		getExtensions().add("runtimeOnly", bucketRegistry.register(identifier.child("runtimeOnly"), DeclarableDependencyBucketSpec.class).get());
	}

	@Override
	public DeclarableDependencyBucketSpec getImplementation() {
		return (DeclarableDependencyBucketSpec) getExtensions().getByName("implementation");
	}

	@Override
	public DeclarableDependencyBucketSpec getRuntimeOnly() {
		return (DeclarableDependencyBucketSpec) getExtensions().getByName("runtimeOnly");
	}
}
