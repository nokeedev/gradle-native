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
package dev.nokee.model.internal.registry;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.type.ModelType;

public final class ModelNodeBackedElement implements ModelElement, ModelNodeAware {
	private final ModelNode node;

	public ModelNodeBackedElement(ModelNode node) {
		this.node = node;
	}

	@Override
	public String getName() {
		return ModelNodeUtils.getPath(node).getName();
	}

	@Override
	public <T> DomainObjectProvider<T> as(ModelType<T> type) {
		return new ModelNodeBackedProvider<>(type, node);
	}

	@Override
	public ModelNode getNode() {
		return node;
	}
}
