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

import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.elements.ComponentElementsPropertyRegistrationFactory;
import lombok.val;

import java.util.function.Supplier;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ComponentDependenciesPropertyRegistrationFactory {
	private final ComponentElementsPropertyRegistrationFactory factory = new ComponentElementsPropertyRegistrationFactory();
	private final ModelLookup lookup;

	public ComponentDependenciesPropertyRegistrationFactory(ModelLookup lookup) {
		this.lookup = lookup;
	}

	public <T extends ComponentDependencies> ModelRegistration create(ModelPropertyIdentifier identifier, Class<T> type, Supplier<? extends T> instance) {
		val path = toPath(identifier);
		assert path.getParent().isPresent();
		val ownerPath = path.getParent().get();
		return ModelRegistration.builder()
			.withComponent(identifier)
			.mergeFrom(factory.newProperty().baseRef(lookup.get(ownerPath)).elementType(of(DependencyBucket.class)).build())
			.withComponent(createdUsing(of(type), instance::get))
			.build();
	}
}
