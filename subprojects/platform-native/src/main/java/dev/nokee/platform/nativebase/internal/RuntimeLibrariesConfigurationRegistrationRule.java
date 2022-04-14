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

import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.resolvable;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;

@SuppressWarnings("rawtypes")
public final class RuntimeLibrariesConfigurationRegistrationRule extends ModelActionWithInputs.ModelAction2<BinaryIdentifier, ModelProjection> {
	private final ModelRegistry registry;
	private final ResolvableDependencyBucketRegistrationFactory resolvableFactory;
	private final ObjectFactory objects;

	public RuntimeLibrariesConfigurationRegistrationRule(ModelRegistry registry, ResolvableDependencyBucketRegistrationFactory resolvableFactory, ObjectFactory objects) {
		super(ModelComponentReference.of(BinaryIdentifier.class), ModelComponentReference.ofProjection(HasRuntimeLibrariesDependencyBucket.class));
		this.registry = registry;
		this.resolvableFactory = resolvableFactory;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, BinaryIdentifier identifier, ModelProjection projection) {
		val runtimeLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("runtimeLibraries"), identifier)));
		runtimeLibraries.configure(Configuration.class, forNativeRuntimeUsage());
		entity.addComponent(new RuntimeLibrariesConfiguration(runtimeLibraries.as(Configuration.class)));
		entity.addComponent(new DependentRuntimeLibraries(runtimeLibraries.as(Configuration.class).flatMap(it -> it.getIncoming().getFiles().getElements())));
	}

	private Action<Configuration> forNativeRuntimeUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.NATIVE_RUNTIME)));
	}
}
