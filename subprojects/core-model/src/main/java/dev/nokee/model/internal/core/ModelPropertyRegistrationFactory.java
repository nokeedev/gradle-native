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
package dev.nokee.model.internal.core;

import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

import java.io.File;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelTypes.set;

public final class ModelPropertyRegistrationFactory {
	private final ModelLookup lookup;
	private final ObjectFactory objects;
	private final ModelConfigurer modelConfigurer;

	public ModelPropertyRegistrationFactory(ModelLookup lookup, ObjectFactory objects, ModelConfigurer modelConfigurer) {
		this.lookup = lookup;
		this.objects = objects;
		this.modelConfigurer = modelConfigurer;
	}

	public ModelRegistration create(ModelPropertyIdentifier identifier, ModelNode entity) {
		assert entity.has(IdentifierComponent.class);
		val path = toPath(identifier);
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(new OriginalEntityComponent(entity))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ModelPropertyTag.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (e, id, tag, ignored) -> {
				// When property is realized... realize the source entity
				if (id.get().equals(identifier)) {
					ModelStates.realize(entity);
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.class), (e, state) -> {
				// When entity is realized by not the property
				if (state.isAtLeast(ModelState.Realized) && entity.getId() == e.getId()) {
					val propertyNode = lookup.find(path);
					if (propertyNode.isPresent()) {
						ModelStates.realize(propertyNode.get());
					} else {
						modelConfigurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ModelPropertyTag.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (ee, id, tag, ignored) -> {
							if (id.get().equals(identifier)) {
								ModelStates.realize(ee);
							}
						}));
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ModelPropertyTag.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (e, id, tag, ignored) -> {
				if (id.get().equals(identifier)) {
					e.addComponent(ModelPropertyTag.instance());
					e.addComponent(new ModelPropertyTypeComponent(ModelType.untyped()));
					e.addComponent(new DelegatedModelProjection(entity));
				}
			}))
			.build();
	}

	public <T> ModelRegistration createProperty(ModelPropertyIdentifier identifier, Class<T> type) {
		val property = objects.property(type);
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(ModelPropertyTag.instance())
			.withComponent(new ModelPropertyTypeComponent(of(type)))
			.withComponent(new GradlePropertyComponent(property))
			.build();
	}

	public <T> ModelRegistration createFileCollectionProperty(ModelPropertyIdentifier identifier) {
		val property = objects.fileCollection();
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(ModelPropertyTag.instance())
			.withComponent(new ModelPropertyTypeComponent(set(of(File.class))))
			.withComponent(new GradlePropertyComponent(property))
			.build();
	}
}
