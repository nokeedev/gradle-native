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
package dev.nokee.platform.base.internal;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.ComponentDependencies;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.artifacts.Configuration;

import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;

public final class ComponentDependenciesPropertyRegistrationFactory {
	private final ModelRegistry registry;
	private final ModelPropertyRegistrationFactory propertyFactory;
	private final ModelConfigurer modelConfigurer;

	public ComponentDependenciesPropertyRegistrationFactory(ModelRegistry registry, ModelPropertyRegistrationFactory propertyFactory, ModelConfigurer modelConfigurer) {
		this.registry = registry;
		this.propertyFactory = propertyFactory;
		this.modelConfigurer = modelConfigurer;
	}

	// TODO: We should accept the property identifier and use that to figure out descendant, path, and finalizing.
	public ModelRegistration create(ModelPath path, ComponentDependencies instance) {
		assert path.getParent().isPresent();
		val ownerPath = path.getParent().get();
		return ModelRegistration.builder()
			.withComponent(path)
			.withComponent(IsModelProperty.tag())
			.withComponent(ofInstance(instance))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (ee, pp, ignored) -> {
				if (path.equals(pp)) {
					modelConfigurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsDependencyBucket.class), ModelComponentReference.ofAny(projectionOf(Configuration.class)), (e, p, ignored1, ignored2, projection) -> {
						if (ownerPath.isDescendant(p)) {
							val elementName = StringUtils.uncapitalize(Streams.stream(Iterables.skip(p, Iterables.size(ownerPath)))
								.filter(it -> !it.isEmpty())
								.map(StringUtils::capitalize)
								.collect(Collectors.joining()));
							registry.register(propertyFactory.create(path.child(elementName), e));
						}
					}));
				}
			}))
			.build();
	}
}
