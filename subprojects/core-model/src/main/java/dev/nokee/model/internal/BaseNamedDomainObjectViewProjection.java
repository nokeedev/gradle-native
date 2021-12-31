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
import dev.nokee.model.NamedDomainObjectView;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.UnaryOperator;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.self;

public class BaseNamedDomainObjectViewProjection implements AbstractModelNodeBackedNamedDomainObjectView.Projection {
	private final ObjectFactory objectFactory;
	private final ModelElementFactory elementFactory;
	private final ModelNode node = getCurrentModelNode();

	@Inject
	public BaseNamedDomainObjectViewProjection(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
		this.elementFactory = new ModelElementFactory();
	}

	@Override
	public <T> DomainObjectProvider<T> get(String name, ModelType<T> type) {
		return elementFactory.createObject(checkType(name, type).apply(ModelNodeUtils.getDescendant(node, name)), type);
	}

	@Override
	public <T> void configure(String name, ModelType<T> type, Action<? super T> action) {
		ModelNodeUtils.applyTo(checkType(name, type).apply(ModelNodeUtils.getDescendant(node, name)), self(stateAtLeast(ModelState.Realized)).apply(executeUsingProjection(type, action)));
	}

//	protected String getTypeDisplayName() {
//		return getType().getSimpleName();
//	}

	protected static InvalidUserDataException createWrongTypeException(String name, Class expected, String actual) {
		return new InvalidUserDataException(String.format("The domain object '%s' (%s) is not a subclass of the given type (%s).", name, actual, expected.getCanonicalName()));
	}

	private static UnaryOperator<ModelNode> checkType(String name, ModelType<?> expected) {
		return node -> {
			if (ModelNodeUtils.canBeViewedAs(node, expected)) {
				return node;
			}
			throw createWrongTypeException(name, expected.getConcreteType(), ModelNodeUtils.getTypeDescription(node).orElse("<unknown>"));
		};
	}

	@Override
	public <T> Map<String, DomainObjectProvider<T>> getAsMap(ModelType<T> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> NamedDomainObjectView<T> createSubView(ModelType<T> type) {
		return (NamedDomainObjectView<T>) objectFactory.newInstance(SubView.class, type, node);
	}

	static class SubView<T> extends BaseNamedDomainObjectView<T> {
		@Inject
		public SubView(ModelType<T> elementType, ModelNode node) {
			super(elementType, node);
		}
	}
}
