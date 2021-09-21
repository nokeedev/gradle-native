/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectContainer;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactories;
import dev.nokee.model.internal.type.ModelType;

import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.type.ModelType.of;

/**
 * Base implementation for domain object container with Groovy support.
 *
 * @param <T>
 */
public class BaseNamedDomainObjectContainer<T> extends AbstractModelNodeBackedNamedDomainObjectContainer<T> implements DomainObjectContainer<T> {
	protected BaseNamedDomainObjectContainer() {
		super(null, getCurrentModelNode());
	}

	protected BaseNamedDomainObjectContainer(Class<T> elementType) {
		super(of(elementType), getCurrentModelNode());
	}

	public static <T> NodeRegistration<T> namedContainer(String name, ModelType<T> viewType) {
		return NodeRegistration.of(name, viewType, elementTypeParameter(viewType, DomainObjectContainer.class))
			.withProjection(managed(of(BaseDomainObjectViewProjection.class)))
			.withProjection(managed(of(BaseNamedDomainObjectViewProjection.class)))
			.withProjection(managed(of(BaseNamedDomainObjectContainerProjection.class)))
			.withProjection(ofInstance(new NodeRegistrationFactories()));
	}
}
