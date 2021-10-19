/*
 * Copyright 2020-2021 the original author or authors.
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

import com.google.common.base.Preconditions;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ProviderUtils;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.self;

@EqualsAndHashCode
public final class ModelNodeBackedProvider<T> implements DomainObjectProvider<T>, ModelNodeAware {
	private final ModelIdentifier<T> identifier;
	private final ModelType<T> type;
	@EqualsAndHashCode.Exclude private final ModelNode node;

	public ModelNodeBackedProvider(ModelType<T> type, ModelNode node) {
		Preconditions.checkArgument(ModelNodeUtils.canBeViewedAs(node, type), "node '%s' cannot be viewed as %s", node, type);
		this.identifier = ModelIdentifier.of(ModelNodeUtils.getPath(node), type);
		this.type = type;
		this.node = node;
	}

	@Override
	public ModelIdentifier<T> getIdentifier() {
		return identifier;
	}

	@Override
	public T get() {
		// TODO: We should prevent realizing the provider before a certain gate is achieved (maybe not registered)
		return ModelNodeUtils.get(ModelStates.realize(node), type);
	}

	@Override
	public Class<T> getType() {
		return type.getConcreteType();
	}

	@Override
	public DomainObjectProvider<T> configure(Action<? super T> action) {
		ModelNodeUtils.applyTo(node, self(stateAtLeast(ModelState.Realized)).apply(once(executeUsingProjection(type, action))));
		return this;
	}

	Provider<T> getAsProvider() {
		return ProviderUtils.supplied(() -> ModelNodeUtils.get(ModelStates.realize(node), type));
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		return getAsProvider().map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return getAsProvider().flatMap(transformer);
	}

	@Override
	public String toString() {
		return "provider(node '" + ModelNodeUtils.getPath(node) + "', " + type + ")";
	}

	@Override
	public ModelNode getNode() {
		return node;
	}

	@Override
	public NamedDomainObjectProvider<T> asProvider() {
		return new ModelBackedNamedDomainObjectProvider<>(this);
	}
}
