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

package dev.nokee.language.swift.internal.plugins;

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import org.gradle.api.plugins.ExtensionAware;

import javax.inject.Inject;

public /*final*/ abstract class DefaultSwiftComponentDependencies implements ComponentDependencies, ExtensionAware {
	@Inject
	public DefaultSwiftComponentDependencies(ModelObjectIdentifier identifier, ModelObjectRegistry<DependencyBucket> bucketRegistry) {
		getExtensions().add("importModules", bucketRegistry.register(identifier.child("importModules"), ResolvableDependencyBucketSpec.class).get());
	}

	public ResolvableDependencyBucketSpec getImportModules() {
		return (ResolvableDependencyBucketSpec) getExtensions().getByName("importModules");
	}
}
