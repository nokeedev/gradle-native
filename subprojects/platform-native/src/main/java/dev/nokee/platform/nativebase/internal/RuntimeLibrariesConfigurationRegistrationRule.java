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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.TypeCompatibilityModelProjectionSupport;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;

public final class RuntimeLibrariesConfigurationRegistrationRule extends ModelActionWithInputs.ModelAction3<IdentifierComponent, ModelComponentTag<IsBinary>, TypeCompatibilityModelProjectionSupport<HasRuntimeLibrariesDependencyBucket>> {
	private final ModelRegistry registry;
	private final ObjectFactory objects;

	public RuntimeLibrariesConfigurationRegistrationRule(ModelRegistry registry, ObjectFactory objects) {
		this.registry = registry;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, IdentifierComponent identifier, ModelComponentTag<IsBinary> ignored, TypeCompatibilityModelProjectionSupport<HasRuntimeLibrariesDependencyBucket> projection) {
		val runtimeLibraries = registry.register(newEntity("runtimeLibraries", ResolvableDependencyBucketSpec.class, it -> it.ownedBy(entity).withTag(RuntimeLibrariesDependencyBucketTag.class)));
		runtimeLibraries.configure(Configuration.class, forNativeRuntimeUsage());
		entity.addComponent(new RuntimeLibrariesConfiguration(ModelNodes.of(runtimeLibraries)));
		entity.addComponent(new DependentRuntimeLibraries(runtimeLibraries.as(Configuration.class).flatMap(it -> it.getIncoming().getFiles().getElements())));
	}

	private Action<Configuration> forNativeRuntimeUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.NATIVE_RUNTIME)));
	}
}
