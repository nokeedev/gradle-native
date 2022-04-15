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

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.type.ModelType;

import java.util.Map;

public abstract class ModelNodeBackedNamedDomainObjectCollectionDynamicObject extends DomainElementsDynamicObject {
	private final ModelType<?> elementType;
	private final ModelNode node;

	protected ModelNodeBackedNamedDomainObjectCollectionDynamicObject(ModelType<?> elementType, ModelNode node) {
		this.elementType = elementType;
		this.node = node;
	}

	@Override
	protected ModelType<?> getElementType() {
		return elementType;
	}

	@Override
	protected boolean hasElement(String name) {
		return ModelNodeUtils.hasDescendant(node, name);
	}

	@Override
	protected DomainObjectProvider<?> getElement(String name, ModelType<?> type) {
		return ModelNodeUtils.get(node, BaseNamedDomainObjectViewProjection.class).get(name, type);
	}

	@Override
	protected Map<String, ? extends DomainObjectProvider<?>> getElementsAsMap() {
		return ModelNodeUtils.get(node, BaseNamedDomainObjectViewProjection.class).getAsMap(getElementType());
	}

	@Override
	protected DomainObjectProvider<?> doRegister(String name, ModelType<?> type) {
		throw new UnsupportedOperationException();
	}
}
