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
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import dev.nokee.platform.base.internal.variants.VariantViewInternal;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelActions.*;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.TransformerUtils.*;

public class ModelNodeBackedVariantView<T extends Variant> implements VariantView<T>, ModelNodeAware {
	private final ProviderFactory providers;
	private final Class<T> elementType;
	private final ModelNode node;

	public ModelNodeBackedVariantView(ProviderFactory providers, Class<T> elementType) {
		this(providers, elementType, ModelNodeContext.getCurrentModelNode());
	}

	private ModelNodeBackedVariantView(ProviderFactory providers, Class<T> elementType, ModelNode node) {
		this.providers = providers;
		this.elementType = elementType;
		this.node = node;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		applyTo(node, allDirectDescendants(stateAtLeast(ModelState.Realized).and(ModelNodes.withType(of(elementType))))
			.apply(once(executeUsingProjection(of(elementType), action))));
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		applyTo(node, allDirectDescendants(stateAtLeast(ModelState.Realized).and(ModelNodes.withType(of(type))))
			.apply(once(executeUsingProjection(of(type), action))));
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		applyTo(node, allDirectDescendants(stateAtLeast(ModelState.Realized).and(ModelNodes.withType(of(elementType))))
			.apply(once(executeUsingProjection(of(elementType), it -> {
				if (spec.isSatisfiedBy(it)) {
					action.execute(it);
				}
			}))));
	}

	@Override
	public <S extends T> VariantView<S> withType(Class<S> type) {
		return new ModelNodeBackedVariantView<>(providers, type, node);
	}

	@Override
	public Provider<Set<T>> getElements() {
		return providers.provider(() -> ModelProperties.getProperties(node).map(it -> it.as(elementType).get()).collect(Collectors.toSet()));
	}

	@Override
	public Set<T> get() {
		return getElements().get();
	}

	@Override
	public <S> Provider<List<S>> map(Transformer<? extends S, ? super T> mapper) {
		return getElements().map(transformEach(mapper).andThen(toListTransformer()));
	}

	@Override
	public <S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super T> mapper) {
		return getElements().map(flatTransformEach(mapper).andThen(toListTransformer()));
	}

	@Override
	public Provider<List<T>> filter(Spec<? super T> spec) {
		return getElements().map(matching(spec).andThen(toListTransformer(elementType)));
	}

	@Override
	public ModelNode getNode() {
		return node;
	}

	public void whenElementKnown(Action<? super KnownDomainObject<T>> action) {
		applyTo(node, allDirectDescendants(stateAtLeast(ModelState.Created).and(ModelNodes.withType(of(elementType))))
			.apply(once(executeAsKnownProjection(of(elementType), action))));
	}

	public <S extends T> void whenElementKnown(Class<S> type, Action<? super KnownDomainObject<S>> action) {
		applyTo(node, allDirectDescendants(stateAtLeast(ModelState.Created).and(ModelNodes.withType(of(type))))
			.apply(once(executeAsKnownProjection(of(type), action))));
	}
}
