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
import dev.nokee.model.DomainObjectProvider;
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

import java.util.Objects;
import java.util.function.Supplier;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.self;

// TODO: implementing ModelNodeAware is simply for legacy reason, it needs to be removed.
@EqualsAndHashCode
public final class DefaultModelObject<T> implements DomainObjectProvider<T>, ProviderConvertibleInternal<T>, ModelNodeAware {
	@EqualsAndHashCode.Exclude private final Supplier<String> nameSupplier;
	private final Supplier<DomainObjectIdentifier> identifierSupplier;
	private final ModelType<T> type;
	@EqualsAndHashCode.Exclude private final ConfigurableProviderConvertibleStrategy providerConvertibleStrategy;
	@EqualsAndHashCode.Exclude private final ConfigurableStrategy configurableStrategy;
	@EqualsAndHashCode.Exclude private final ModelCastableStrategy castableStrategy;
	@EqualsAndHashCode.Exclude private final ModelPropertyLookupStrategy propertyLookup;
	@EqualsAndHashCode.Exclude private final Supplier<? extends T> valueSupplier;
	@EqualsAndHashCode.Exclude private final Supplier<ModelNode> entitySupplier;

	public DefaultModelObject(Supplier<String> nameSupplier, Supplier<DomainObjectIdentifier> identifierSupplier, ModelType<T> type, ConfigurableProviderConvertibleStrategy providerConvertibleStrategy, ConfigurableStrategy configurableStrategy, ModelCastableStrategy castableStrategy, ModelPropertyLookupStrategy propertyLookup, Supplier<? extends T> valueSupplier, Supplier<ModelNode> entitySupplier) {
		this.nameSupplier = Objects.requireNonNull(nameSupplier);
		this.identifierSupplier = Objects.requireNonNull(identifierSupplier);
		this.type = Objects.requireNonNull(type);
		this.providerConvertibleStrategy = Objects.requireNonNull(providerConvertibleStrategy);
		this.configurableStrategy = Objects.requireNonNull(configurableStrategy);
		this.castableStrategy = Objects.requireNonNull(castableStrategy);
		this.propertyLookup = Objects.requireNonNull(propertyLookup);
		this.valueSupplier = Objects.requireNonNull(valueSupplier);
		this.entitySupplier = Objects.requireNonNull(entitySupplier);
	}

	public static <T> DefaultModelObject<T> of(ModelType<T> type, ModelNode entity) {
		// TODO: Align exception with the one in ModelNode#get(ModelType). It's throwing an illegal state exception...
		Preconditions.checkArgument(ModelNodeUtils.canBeViewedAs(entity, type), "node '%s' cannot be viewed as %s", entity, type);
		@SuppressWarnings("unchecked")
		val fullType = (ModelType<T>) entity.getComponents().filter(ModelProjection.class::isInstance).map(ModelProjection.class::cast).filter(it -> it.canBeViewedAs(type)).map(ModelProjection::getType).findFirst().orElseThrow(RuntimeException::new);
		val nameSupplier = entity.getComponent(ElementNameComponent.class);
		val castableStrategy = new ModelBackedModelCastableStrategy(entity);
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
		return new DefaultModelObject<>(nameSupplier, identifierSupplier, fullType, providerStrategy, configurableStrategy, castableStrategy, propertyLookup, valueSupplier, () -> entity);
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
	public DomainObjectIdentifier getIdentifier() {
		return identifierSupplier.get();
	}

	@Override
	public Class<T> getType() {
		return type.getConcreteType();
	}

	@Override
	public DefaultModelObject<T> configure(Action<? super T> action) {
		Objects.requireNonNull(action);
		configurableStrategy.configure(type, action);
		return this;
	}

	@Override
	public DomainObjectProvider<T> configure(@SuppressWarnings("rawtypes") Closure closure) {
		Objects.requireNonNull(closure);
		configurableStrategy.configure(type, new ClosureWrappedConfigureAction<>(closure));
		return this;
	}

	@Override
	public T get() {
		return valueSupplier.get();
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
		Objects.requireNonNull(transformer);
		return asProvider().map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		Objects.requireNonNull(transformer);
		return asProvider().flatMap(transformer);
	}

	@Override
	public NamedDomainObjectProvider<T> asProvider() {
		return providerConvertibleStrategy.asProvider(type);
	}

	@Override
	public <S> DomainObjectProvider<S> as(ModelType<S> type) {
		Objects.requireNonNull(type);
		return castableStrategy.castTo(type);
	}

	public <S> S asType(Class<S> ignored) {
		throw new UnsupportedOperationException("Use ModelObject#as(ModelType) instead.");
	}

	@Override
	public boolean instanceOf(ModelType<?> type) {
		Objects.requireNonNull(type);
		return castableStrategy.instanceOf(type);
	}

	@Override
	public ModelElement property(String name) {
		Objects.requireNonNull(name);
		return propertyLookup.get(name);
	}

	@Override
	public <S> DefaultModelObject<T> configure(ModelType<S> type, Action<? super S> action) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(action);
		configurableStrategy.configure(type, action);
		return this;
	}

	@Override
	public ModelNode getNode() {
		return entitySupplier.get();
	}

	@Override
	public String getName() {
		return nameSupplier.get();
	}
}
