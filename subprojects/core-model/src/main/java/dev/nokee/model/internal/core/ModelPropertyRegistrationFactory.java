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

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

public final class ModelPropertyRegistrationFactory {
	private final ModelLookup lookup;
	private final ObjectFactory objects;

	public ModelPropertyRegistrationFactory(ModelLookup lookup, ObjectFactory objects) {
		this.lookup = lookup;
		this.objects = objects;
	}

	public ModelRegistration create(ModelPropertyIdentifier identifier, ModelNode entity) {
		assert entity.hasComponent(DomainObjectIdentifier.class);
		val path = toPath(identifier);
		return ModelRegistration.builder()
			.withComponent(identifier)
			.withComponent(path)
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPropertyIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (e, id, ignored) -> {
				if (id.equals(identifier)) {
					ModelStates.realize(entity);
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.IsAtLeastRealized.class), new ModelActionWithInputs.A1<ModelState.IsAtLeastRealized>() {
				private boolean realizePropertyWhenPresent = false;

				@Override
				public void execute(ModelNode e, ModelState.IsAtLeastRealized ignored) {
					if (entity.getId() == e.getId()) {
						val propertyNode = lookup.find(path);
						if (propertyNode.isPresent()) {
							ModelStates.realize(propertyNode.get());
						} else {
							realizePropertyWhenPresent = true;
						}
					} else if (realizePropertyWhenPresent && e.hasComponent(ModelPropertyIdentifier.class) && e.getComponent(ModelPropertyIdentifier.class).equals(identifier)) {
						ModelStates.realize(e);
						realizePropertyWhenPresent = false;
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPropertyIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (e, id, ignored) -> {
				if (id.equals(identifier)) {
					e.addComponent(IsModelProperty.tag());
					e.addComponent(new DelegatedModelProjection(entity));
				}
			}))
			.build();
	}

	public <T> ModelRegistration createProperty(ModelPropertyIdentifier identifier, Class<T> type) {
		val path = toPath(identifier);
		val property = objects.property(type);
		return ModelRegistration.builder()
			.withComponent(identifier)
			.withComponent(path)
			.withComponent(IsModelProperty.tag())
			.withComponent(createdUsing(ModelType.of(type), property::getOrNull))
			.withComponent(createdUsing(ModelType.of(Property.class), () -> property))
			.build();
	}
}
