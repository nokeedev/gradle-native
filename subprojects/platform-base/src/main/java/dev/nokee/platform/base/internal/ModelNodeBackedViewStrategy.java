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

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.state.ModelState;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.util.Set;

import static dev.nokee.model.internal.core.ModelActions.*;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ModelNodeBackedViewStrategy implements ViewAdapter.Strategy {
	private static final Runnable NO_OP_REALIZE = () -> {};
	private final Runnable realize;
	private final ModelNode entity;
	private final ProviderFactory providerFactory;

	public ModelNodeBackedViewStrategy(ProviderFactory providerFactory) {
		this.providerFactory = providerFactory;
		this.entity = ModelNodeContext.getCurrentModelNode();
		this.realize = NO_OP_REALIZE;
	}

	public ModelNodeBackedViewStrategy(ProviderFactory providerFactory, Runnable realize) {
		this(providerFactory, realize, ModelNodeContext.getCurrentModelNode());
	}

	public ModelNodeBackedViewStrategy(ProviderFactory providerFactory, Runnable realize, ModelNode entity) {
		this.providerFactory = providerFactory;
		this.realize = new RunOnceRunnable(realize);
		this.entity = entity;
	}

	@Override
	public <T> void configureEach(Class<T> elementType, Action<? super T> action) {
		applyTo(entity, allDirectDescendants(stateAtLeast(ModelState.Realized).and(ModelNodes.withType(of(elementType))))
			.apply(once(executeUsingProjection(of(elementType), action))));
	}

	@Override
	public <T> Provider<Set<T>> getElements(Class<T> elementType) {
		return providerFactory.provider(() -> {
			realize.run();
			return ModelProperties.getProperties(entity).filter(it -> it.instanceOf(elementType)).map(it -> it.as(elementType).get()).collect(ImmutableSet.toImmutableSet());
		});
	}

	@Override
	public <T> void whenElementKnown(Class<T> elementType, Action<? super KnownDomainObject<T>> action) {
		applyTo(entity, allDirectDescendants(stateAtLeast(ModelState.Created).and(ModelNodes.withType(of(elementType))))
			.apply(once(executeAsKnownProjection(of(elementType), action))));
	}

	private static final class RunOnceRunnable implements Runnable {
		private Runnable delegate;

		public RunOnceRunnable(Runnable delegate) {
			this.delegate = delegate;
		}

		@Override
		public void run() {
			if (delegate != null) {
				val runnable = delegate;
				delegate = null;
				runnable.run();
			}
		}
	}
}
