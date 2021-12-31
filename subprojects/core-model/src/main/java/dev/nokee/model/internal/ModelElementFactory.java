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
import dev.nokee.gradle.TaskProviderFactory;
import dev.nokee.internal.reflect.Instantiator;
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
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.NodePredicate.self;

public final class ModelElementFactory {
	private final Instantiator instantiator;

	public ModelElementFactory(Instantiator instantiator) {
		this.instantiator = instantiator;
	}

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
		val elementLookup = new ModelBackedModelElementLookupStrategy(entity, this);
		val mixInStrategy = new ModelMixInStrategy() {
			@Override
			public <S> DomainObjectProvider<S> mixin(ModelType<S> type) {
				if (castableTypes(entity).anyMatch(type::isSupertypeOf)) {
					throw new RuntimeException();
				}
				entity.addComponent(createdUsing(type, () -> instantiator.newInstance(type.getConcreteType())));
				return castableStrategy.castTo(type);
			}
		};
		return new DefaultModelElement(
			namedStrategy,
			configurableStrategy,
			castableStrategy,
			propertyLookup,
			elementLookup,
			mixInStrategy,
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
		val elementLookup = new ModelBackedModelElementLookupStrategy(entity, this);
		val mixInStrategy = new ModelMixInStrategy() {
			@Override
			public <S> DomainObjectProvider<S> mixin(ModelType<S> type) {
				if (castableTypes(entity).anyMatch(type::isSupertypeOf)) {
					throw new RuntimeException();
				}
				entity.addComponent(createdUsing(type, () -> instantiator.newInstance(type.getConcreteType())));
				return castableStrategy.castTo(type);
			}
		};

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
		val provider = entity.findComponent(ModelElementProviderSourceComponent.class).map(it -> it.get().map(ignored -> ModelNodeUtils.get(entity, type))).orElseGet(() -> ProviderUtils.supplied(() -> ModelNodeUtils.get(entity, type))).map(it -> {
			if (!type.isSubtypeOf(Provider.class)) {
				ModelStates.realize(entity);
			}
			return it;
		});
		val p = provider;
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
						return factoryFor(t).create(NamedDomainObjectProviderSpec.builder().named(() -> entity.getComponent(FullyQualifiedNameComponent.class).get().toString()).delegateTo(p).typedAs(t.getConcreteType()).configureUsing(action -> configurableStrategy.configure(t, action)).build());
					}
				} else {
					return factoryFor(t).create(NamedDomainObjectProviderSpec.builder().named(() -> entity.getComponent(FullyQualifiedNameComponent.class).get().toString()).delegateTo(p).typedAs(t.getConcreteType()).configureUsing(action -> configurableStrategy.configure(t, action)).build());
				}
			}
		};
		val identifierSupplier = new IdentifierSupplier(entity, fullType);
		return new DefaultModelObject<>(namedStrategy, identifierSupplier, fullType, providerStrategy, configurableStrategy, castableStrategy, propertyLookup, elementLookup, mixInStrategy, valueSupplier, () -> entity);
	}

	interface ConfigurableProviderFactory {
		<T> NamedDomainObjectProvider<T> create(NamedDomainObjectProviderSpec<T> spec);
	}

	private static ConfigurableProviderFactory factoryFor(ModelType<?> type) {
		if (Task.class.isAssignableFrom(type.getRawType())) {
			return new ConfigurableProviderFactory() {
				@Override
				@SuppressWarnings("unchecked")
				public <T> NamedDomainObjectProvider<T> create(NamedDomainObjectProviderSpec<T> spec) {
					val t = (NamedDomainObjectProviderSpec<? extends Task>) spec;
					val factory = new TaskProviderFactory();
					return (NamedDomainObjectProvider<T>) factory.create(t);
				}
			};
		} else {
			return new NamedDomainObjectProviderFactory()::create;
		}
	}

	public <T> ModelProperty<T> createProperty(ModelNode entity, ModelType<T> type) {
		Preconditions.checkArgument(entity.hasComponent(ModelPropertyTag.class));
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
		val elementLookup = new ModelBackedModelElementLookupStrategy(entity, this);
		val mixInStrategy = new ModelMixInStrategy() {
			@Override
			public <S> DomainObjectProvider<S> mixin(ModelType<S> type) {
				if (castableTypes(entity).anyMatch(type::isSupertypeOf)) {
					throw new RuntimeException();
				}
				entity.addComponent(createdUsing(type, () -> instantiator.newInstance(type.getConcreteType())));
				return castableStrategy.castTo(type);
			}
		};

		val valueSupplier = new GradlePropertyBackedValueSupplier<T>(entity);
		val provider = ProviderUtils.supplied(valueSupplier::get);
		val p = provider;
		val factory = new NamedDomainObjectProviderFactory();
		val propertyStrategy = new ModelBackedGradlePropertyConvertibleStrategy(entity);
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
						return factory.create(NamedDomainObjectProviderSpec.builder().named(() -> entity.getComponent(FullyQualifiedNameComponent.class).get().toString()).delegateTo(p).typedAs(t.getConcreteType()).configureUsing(action -> configurableStrategy.configure(t, action)).build());
					}
				} else {
					return factory.create(NamedDomainObjectProviderSpec.builder().named(() -> entity.getComponent(FullyQualifiedNameComponent.class).get().toString()).delegateTo(p).typedAs(t.getConcreteType()).configureUsing(action -> configurableStrategy.configure(t, action)).build());
				}
			}
		};
		val identifierSupplier = new IdentifierSupplier(entity, fullType);
		return new DefaultModelProperty<T>(namedStrategy, identifierSupplier, fullType, providerStrategy, propertyStrategy, configurableStrategy, castableStrategy, propertyLookup, elementLookup, mixInStrategy, valueSupplier, () -> entity);
	}

	private static final class GradlePropertyBackedValueSupplier<T> implements Supplier<T> {
		private final ModelNode entity;

		private GradlePropertyBackedValueSupplier(ModelNode entity) {
			this.entity = entity;
		}

		@Override
		public T get() {
			val value = entity.getComponent(GradlePropertyComponent.class).get();
			if (value instanceof Provider) {
				return ((Provider<T>) value).get();
			} else if (value instanceof FileCollection) {
				return (T) ((FileCollection) value).getFiles(); // FIXME: Value supplier should pass the expected value out
			} else {
				throw new UnsupportedOperationException("Cannot get value out of property");
			}
		}
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

	private static Stream<ModelType<?>> castableTypes(ModelNode entity) {
		return ModelNodeUtils.getProjections(entity).map(ModelProjection::getType);
	}
}
