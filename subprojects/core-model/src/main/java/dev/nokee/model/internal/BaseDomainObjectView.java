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

import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.type.ModelType;

import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;
import static java.util.Objects.requireNonNull;

public class BaseDomainObjectView<T> extends AbstractModelNodeBackedDomainObjectView<T> implements DomainObjectView<T> {
	protected BaseDomainObjectView() {
		super(null, getCurrentModelNode());
	}

	protected BaseDomainObjectView(Class<T> elementType) {
		super(of(elementType), getCurrentModelNode());
	}

	// Don't share beyond package
	BaseDomainObjectView(ModelType<T> elementType, ModelNode node) {
		super(requireNonNull(elementType), node);
	}

	public static <T> NodeRegistration view(String name, ModelType<T> viewType) {
		return NodeRegistration.of(name, viewType, elementTypeParameter(viewType, DomainObjectView.class))
			.withComponent(managed(of(BaseDomainObjectViewProjection.class)));
	}
}
