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

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.Set;

import static dev.nokee.model.internal.core.ModelActions.*;
import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;

public class BaseDomainObjectViewProjection implements AbstractModelNodeBackedDomainObjectView.Projection {
	private final ProviderFactory providerFactory;
	private final ObjectFactory objectFactory;
	private final ModelNode node = getCurrentModelNode();

	@Inject
	public BaseDomainObjectViewProjection(ProviderFactory providerFactory, ObjectFactory objectFactory) {
		this.providerFactory = providerFactory;
		this.objectFactory = objectFactory;
	}

	@Override
	public <T> void configureEach(ModelType<T> type, Action<? super T> action) {
		node.applyTo(allDirectDescendants(stateAtLeast(ModelNode.State.Realized).and(withType(type))).apply(executeUsingProjection(type, action)));
	}

	@Override
	public <T> void configureEach(ModelType<T> type, Spec<? super T> spec, Action<? super T> action) {
		node.applyTo(allDirectDescendants(stateAtLeast(ModelNode.State.Realized).and(withType(type)).and(isSatisfiedByProjection(type, spec))).apply(executeUsingProjection(type, action)));
	}

	private <T> Set<T> get(ModelType<T> type) {
		return ModelNodeUtils.getDirectDescendants(node)
			.stream()
			.filter(it -> it.canBeViewedAs(type))
			.map(it -> ModelNodeUtils.realize(it).get(type))
			.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public <T> Provider<Set<T>> getElements(ModelType<T> type) {
		return providerFactory.provider(() -> get(type));
	}

	@Override
	public <T> void whenElementKnown(ModelType<T> type, Action<? super KnownDomainObject<T>> action) {
		node.applyTo(allDirectDescendants(stateAtLeast(ModelNode.State.Registered).and(withType(type))).apply(once(executeAsKnownProjection(type, action))));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> DomainObjectView<T> createSubView(ModelType<T> type) {
		return (DomainObjectView<T>) objectFactory.newInstance(SubView.class, type, node);
	}

	static class SubView<T> extends BaseDomainObjectView<T> {
		@Inject
		public SubView(ModelType<T> elementType, ModelNode node) {
			super(elementType, node);
		}
	}
}
