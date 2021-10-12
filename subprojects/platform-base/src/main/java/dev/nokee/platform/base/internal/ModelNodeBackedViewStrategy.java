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

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.state.ModelState;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.util.Set;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelActions.*;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ModelNodeBackedViewStrategy implements ViewAdapter.Strategy {
	private final ModelNode entity;
	private final ProviderFactory providerFactory;

	public ModelNodeBackedViewStrategy(ProviderFactory providerFactory) {
		this(providerFactory, ModelNodeContext.getCurrentModelNode());
	}

	public ModelNodeBackedViewStrategy(ProviderFactory providerFactory, ModelNode entity) {
		this.providerFactory = providerFactory;
		this.entity = entity;
	}

	@Override
	public <T> void configureEach(Class<T> elementType, Action<? super T> action) {
		applyTo(entity, allDirectDescendants(stateAtLeast(ModelState.Realized).and(ModelNodes.withType(of(elementType))))
			.apply(once(executeUsingProjection(of(elementType), action))));
	}

	@Override
	public <T> Provider<Set<T>> getElements(Class<T> elementType) {
		return providerFactory.provider(() -> ModelProperties.getProperties(entity).map(it -> it.as(elementType).get()).collect(Collectors.toSet()));
	}

	@Override
	public <T> void whenElementKnown(Class<T> elementType, Action<? super KnownDomainObject<T>> action) {
		applyTo(entity, allDirectDescendants(stateAtLeast(ModelState.Created).and(ModelNodes.withType(of(elementType))))
			.apply(once(executeAsKnownProjection(of(elementType), action))));
	}
}
