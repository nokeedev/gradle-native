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
package dev.nokee.model.internal;

import com.google.common.base.Preconditions;
import dev.nokee.gradle.NamedDomainObjectProviderFactory;
import dev.nokee.gradle.NamedDomainObjectProviderSpec;
import dev.nokee.internal.provider.ProviderConvertibleInternal;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import dev.nokee.utils.ProviderUtils;
import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import java.util.function.Supplier;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class DefaultKnownDomainObject<T> implements KnownDomainObject<T>, ProviderConvertibleInternal<T> {
	private final Supplier<DomainObjectIdentifier> identifierSupplier;
	private final ModelType<T> type;
	private final ConfigurableProviderConvertibleStrategy providerConvertibleStrategy;
	private final ConfigurableStrategy configurableStrategy;

	public DefaultKnownDomainObject(Supplier<DomainObjectIdentifier> identifierSupplier, ModelType<T> type, ConfigurableProviderConvertibleStrategy providerConvertibleStrategy, ConfigurableStrategy configurableStrategy) {
		this.identifierSupplier = requireNonNull(identifierSupplier);
		this.type = requireNonNull(type);
		this.providerConvertibleStrategy = requireNonNull(providerConvertibleStrategy);
		this.configurableStrategy = requireNonNull(configurableStrategy);
	}

	public static <T> DefaultKnownDomainObject<T> of(ModelType<T> type, ModelNode entity) {
		// TODO: Align exception with the one in ModelNode#get(ModelType). It's throwing an illegal state exception...
		Preconditions.checkArgument(ModelNodeUtils.canBeViewedAs(entity, type), "node '%s' cannot be viewed as %s", entity, type);
		@SuppressWarnings("unchecked")
		val fullType = (ModelType<T>) entity.getComponents().filter(ModelProjection.class::isInstance).map(ModelProjection.class::cast).map(ModelProjection::getType).filter(it -> it.isSubtypeOf(type)).findFirst().orElseThrow(RuntimeException::new);
		val provider = ProviderUtils.supplied(() -> ModelNodeUtils.get(ModelStates.realize(entity), type));
		val configurableStrategy = new ConfigurableStrategy() {
			@Override
			public <S> void configure(ModelType<S> t, Action<? super S> action) {
				assert fullType.equals(t);
				ModelNodeUtils.applyTo(entity, self(stateAtLeast(ModelState.Realized)).apply(once(executeUsingProjection(t, action))));
			}
		};
		val factory = new NamedDomainObjectProviderFactory();
		val delegate = factory.create(NamedDomainObjectProviderSpec.builder().named(() -> entity.getComponent(FullyQualifiedNameComponent.class).get().toString()).typedAs(fullType.getConcreteType()).delegateTo(provider).configureUsing(action -> configurableStrategy.configure(fullType, action)).build());
		val providerStrategy = new ConfigurableProviderConvertibleStrategy() {
			@Override
			public <S> NamedDomainObjectProvider<S> asProvider(ModelType<S> t) {
				assert fullType.equals(t);
				return (NamedDomainObjectProvider<S>) delegate;
			}
		};
		val identifierSupplier = new IdentifierSupplier(entity, fullType);
		return new DefaultKnownDomainObject<>(identifierSupplier, fullType, providerStrategy, configurableStrategy);
	}

	@EqualsAndHashCode
	private static final class IdentifierSupplier implements Supplier<DomainObjectIdentifier> {
		private final ModelNode entity;
		private final ModelType<?> type;

		private IdentifierSupplier(ModelNode entity, ModelType<?> type) {
			this.entity = entity;
			this.type = type;
		}

		@Override
		public DomainObjectIdentifier get() {
			return entity.findComponent(DomainObjectIdentifier.class).orElseGet(() -> ModelIdentifier.of(entity.getComponent(ModelPath.class), type));
		}
	}

	@Override
	public NamedDomainObjectProvider<T> asProvider() {
		return providerConvertibleStrategy.asProvider(type);
	}

	@Override
	@EqualsAndHashCode.Include(replaces = "identifierSupplier")
	public DomainObjectIdentifier getIdentifier() {
		return identifierSupplier.get();
	}

	@Override
	@EqualsAndHashCode.Include(replaces = "type")
	public Class<T> getType() {
		return type.getConcreteType();
	}

	@Override
	public KnownDomainObject<T> configure(Action<? super T> action) {
		configurableStrategy.configure(type, requireNonNull(action));
		return this;
	}

	@Override
	public KnownDomainObject<T> configure(@SuppressWarnings("rawtypes") Closure closure) {
		configurableStrategy.configure(type, new ClosureWrappedConfigureAction<>(closure));
		return this;
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> mapper) {
		return asProvider().map(requireNonNull(mapper));
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> mapper) {
		return asProvider().flatMap(requireNonNull(mapper));
	}
}
