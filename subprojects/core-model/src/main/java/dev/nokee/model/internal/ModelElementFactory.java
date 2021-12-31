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
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ProviderUtils;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.Objects;
import java.util.function.Supplier;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.self;

public final class ModelElementFactory {
	public ModelElement createElement(ModelNode entity) {
		Objects.requireNonNull(entity);
		val namedStrategy = new NamedStrategy() {
			@Override
			public String getAsString() {
				return entity.getComponent(ElementNameComponent.class).get();
			}
		};
		val castableStrategy = new ModelBackedModelCastableStrategy(entity, this);
		val configurableStrategy = new ConfigurableStrategy() {
			@Override
			public <S> void configure(ModelType<S> type, Action<? super S> action) {
				assert type != null;
				assert action != null;
				if (!ModelNodeUtils.canBeViewedAs(entity, type)) {
					throw new RuntimeException("...");
				}
				if (type.isSubtypeOf(Property.class)) {
					action.execute(castableStrategy.castTo(type).get());
				} else {
					castableStrategy.castTo(type).configure(action);
				}
			}
		};
		val propertyLookup = new ModelBackedModelPropertyLookupStrategy(entity);
		return new DefaultModelElement(
			namedStrategy,
			configurableStrategy,
			castableStrategy,
			propertyLookup,
			() -> entity
		);
	}

	public <T> DomainObjectProvider<T> createObject(ModelNode entity, ModelType<T> type) {
		// TODO: Align exception with the one in ModelNode#get(ModelType). It's throwing an illegal state exception...
		Preconditions.checkArgument(ModelNodeUtils.canBeViewedAs(entity, type), "node '%s' cannot be viewed as %s", entity, type);
		@SuppressWarnings("unchecked")
		val fullType = (ModelType<T>) entity.getComponents().filter(ModelProjection.class::isInstance).map(ModelProjection.class::cast).filter(it -> it.canBeViewedAs(type)).map(ModelProjection::getType).findFirst().orElseThrow(RuntimeException::new);
		val namedStrategy = new NamedStrategy() {
			@Override
			public String getAsString() {
				return entity.getComponent(ElementNameComponent.class).get();
			}
		};
		val castableStrategy = new ModelBackedModelCastableStrategy(entity, this);
		val configurableStrategy = new ConfigurableStrategy() {
			@Override
			public <S> void configure(ModelType<S> type, Action<? super S> action) {
				if (!ModelNodeUtils.canBeViewedAs(entity, type)) {
					throw new RuntimeException("...");
				}
				assert fullType.equals(type);
				val o = entity.getComponents().filter(it -> it instanceof ModelProjection).map(ModelProjection.class::cast).filter(it -> it.canBeViewedAs(ModelType.of(NamedDomainObjectProvider.class))).findFirst().map(it -> it.get(ModelType.of(NamedDomainObjectProvider.class)));
				if (o.isPresent() && ((Boolean) ProviderUtils.getType(o.get()).map(it -> it.equals(type.getConcreteType())).orElse(Boolean.FALSE))) {
					o.get().configure(action);
					return;
				}
				if (entity.hasComponent(projectionOf(NamedDomainObjectProvider.class))) {
					val provider = entity.getComponent(projectionOf(NamedDomainObjectProvider.class)).get(ModelType.of(NamedDomainObjectProvider.class));
					val ttype = ProviderUtils.getType(provider);
					if (ttype.isPresent() && type.getConcreteType().isAssignableFrom((Class<?>) ttype.get())) {
						provider.configure(action);
					} else {
						ModelNodeUtils.applyTo(entity, self(stateAtLeast(ModelState.Realized)).apply(once(executeUsingProjection(type, action))));
					}
				} else {
					ModelNodeUtils.applyTo(entity, self(stateAtLeast(ModelState.Realized)).apply(once(executeUsingProjection(type, action))));
				}
			}
		};
		val propertyLookup = new ModelBackedModelPropertyLookupStrategy(entity);

		val valueSupplier = new Supplier<T>() {
			@Override
			public T get() {
				// TODO: We should prevent realizing the provider before a certain gate is achieved (maybe not registered)
				if (type.isSubtypeOf(Provider.class)) {
					return ModelNodeUtils.get(entity, type);
				}
				return ModelNodeUtils.get(ModelStates.realize(entity), type);
			}
		};
		val provider = ProviderUtils.supplied(valueSupplier::get);
		val p = provider;
		val factory = new NamedDomainObjectProviderFactory();
		val providerStrategy = new ConfigurableProviderConvertibleStrategy() {
			@Override
			public <S> NamedDomainObjectProvider<S> asProvider(ModelType<S> t) {
				assert fullType.equals(t);
				if (entity.hasComponent(projectionOf(NamedDomainObjectProvider.class))) {
					val provider = entity.getComponent(projectionOf(NamedDomainObjectProvider.class)).get(ModelType.of(NamedDomainObjectProvider.class));
					val ttype = ProviderUtils.getType(provider);
					if (ttype.isPresent() && type.getConcreteType().isAssignableFrom((Class<?>) ttype.get())) {
						return provider;
					} else {
						return factory.create(NamedDomainObjectProviderSpec.builder().named(() -> entity.getComponent(FullyQualifiedNameComponent.class).get()).delegateTo(p).typedAs(t.getConcreteType()).configureUsing(action -> configurableStrategy.configure(t, action)).build());
					}
				} else {
					return factory.create(NamedDomainObjectProviderSpec.builder().named(() -> entity.getComponent(FullyQualifiedNameComponent.class).get()).delegateTo(p).typedAs(t.getConcreteType()).configureUsing(action -> configurableStrategy.configure(t, action)).build());
				}
			}
		};
		val identifierSupplier = new IdentifierSupplier(entity, fullType);
		return new DefaultModelObject<>(namedStrategy, identifierSupplier, fullType, providerStrategy, configurableStrategy, castableStrategy, propertyLookup, valueSupplier, () -> entity);
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
}
