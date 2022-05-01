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
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.DefaultModelElement;
import dev.nokee.model.internal.core.DefaultModelProperty;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelBackedModelCastableStrategy;
import dev.nokee.model.internal.core.ModelBackedModelElementLookupStrategy;
import dev.nokee.model.internal.core.ModelBackedModelPropertyLookupStrategy;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelMixInStrategy;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelProperty;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.core.ModelPropertyTypeComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ProviderUtils;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

public final class ModelElementFactory implements ModelComponent {
	private static final Object IGNORED_OBJECT = new Object();
	private final Instantiator instantiator;

	public ModelElementFactory(Instantiator instantiator) {
		this.instantiator = Objects.requireNonNull(instantiator);
	}

	public ModelElement createElement(ModelNode entity) {
		Objects.requireNonNull(entity);
		if (entity.has(ModelPropertyTag.class)) {
			return createPropertyInternal(entity, entity.get(ModelPropertyTypeComponent.class).get());
		} else {
			return createElementInternal(entity);
		}
	}

	private ModelElement createElementInternal(ModelNode entity) {
		Objects.requireNonNull(entity);
		val namedStrategy = new NamedStrategy() {
			@Override
			public String getAsString() {
				return entity.get(ElementNameComponent.class).get().toString();
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
		Objects.requireNonNull(entity);
		Objects.requireNonNull(type);
		if (entity.has(ModelPropertyTag.class) && type.isSupertypeOf(propertyType(entity))) {
			return createPropertyInternal(entity, propertyType(entity));
		} else {
			Preconditions.checkArgument(ModelNodeUtils.canBeViewedAs(entity, type), "node '%s' cannot be viewed as %s", entity, type);
			return createObjectInternal(entity, type);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> ModelType<T> propertyType(ModelNode entity) {
		return (ModelType<T>) entity.get(ModelPropertyTypeComponent.class).get();
	}

	private <T> DomainObjectProvider<T> createObjectInternal(ModelNode entity, ModelType<T> type) {
		// TODO: Align exception with the one in ModelNode#get(ModelType). It's throwing an illegal state exception...
		Preconditions.checkArgument(ModelNodeUtils.canBeViewedAs(entity, type), "node '%s' cannot be viewed as %s", entity, type);
		@SuppressWarnings("unchecked")
		val fullType = (ModelType<T>) ModelNodeUtils.getProjections(entity).filter(it -> it.canBeViewedAs(type)).map(ModelProjection::getType).findFirst().orElseThrow(RuntimeException::new);
		val namedStrategy = new NamedStrategy() {
			@Override
			public String getAsString() {
				return entity.get(ElementNameComponent.class).get().toString();
			}
		};
		val castableStrategy = new ModelBackedModelCastableStrategy(entity, this);
		val configurableStrategy = new ConfigurableStrategy() {
			@Override
			public <S> void configure(ModelType<S> type, Action<? super S> action) {
				if (!ModelNodeUtils.canBeViewedAs(entity, type) && !type.isSupertypeOf(entity.get(ModelPropertyTypeComponent.class).get())) {
					throw new RuntimeException("...");
				}
				assert fullType.equals(type);
				ModelNodeUtils.instantiate(entity, ModelAction.configure(entity.getId(), type.getConcreteType(), action));
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
		val provider = ProviderUtils.supplied(() -> {
			if (!type.isSubtypeOf(Provider.class)) {
				ModelStates.realize(entity);
			}
			return IGNORED_OBJECT;
		}).flatMap(ignored0 -> {
			return entity.find(ModelElementProviderSourceComponent.class)
				.map(component -> component.get().map(ignored1 -> ModelNodeUtils.get(entity, type)))
				.orElseGet(() -> ProviderUtils.supplied(() -> ModelNodeUtils.get(entity, type)));
		});
		val p = provider;
		val providerStrategy = new ConfigurableProviderConvertibleStrategy() {
			@Override
			public <S> NamedDomainObjectProvider<S> asProvider(ModelType<S> t) {
				assert fullType.equals(t);
				return factoryFor(t).create(NamedDomainObjectProviderSpec.builder().named(() -> entity.get(FullyQualifiedNameComponent.class).get().toString()).delegateTo(p).typedAs(t.getConcreteType()).configureUsing(action -> configurableStrategy.configure(t, action)).build());
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

	public ModelProperty<?> createProperty(ModelNode entity) {
		Objects.requireNonNull(entity);
		Preconditions.checkArgument(entity.has(ModelPropertyTag.class));
		return createPropertyInternal(entity, entity.get(ModelPropertyTypeComponent.class).get());
	}

	public <T> ModelProperty<T> createProperty(ModelNode entity, ModelType<T> type) {
		Objects.requireNonNull(entity);
		Objects.requireNonNull(type);
		Preconditions.checkArgument(entity.has(ModelPropertyTag.class));
		Preconditions.checkArgument(type.isSupertypeOf(propertyType(entity)));
		return createPropertyInternal(entity, propertyType(entity));
	}

	private <T> ModelProperty<T> createPropertyInternal(ModelNode entity, ModelType<T> type) {
		val namedStrategy = new NamedStrategy() {
			@Override
			public String getAsString() {
				return entity.get(ElementNameComponent.class).get().toString();
			}
		};
		val castableStrategy = new ModelBackedModelCastableStrategy(entity, this);
		val configurableStrategy = new ConfigurableStrategy() {
			@Override
			public <S> void configure(ModelType<S> type, Action<? super S> action) {
				if (!ModelNodeUtils.canBeViewedAs(entity, type)) {
					throw new RuntimeException("...");
				}
				if (type.isSubtypeOf(Property.class) || type.isSubtypeOf(ConfigurableFileCollection.class)) {
					action.execute(ModelNodeUtils.get(entity, type));
					return;
				}
				ModelNodeUtils.instantiate(entity, ModelAction.configure(entity.getId(), type.getConcreteType(), action));
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
		val provider = ProviderUtils.supplied(() -> {
			if (!type.isSubtypeOf(Provider.class)) {
				ModelStates.realize(entity);
			}
			return IGNORED_OBJECT;
		}).flatMap(ignored0 -> {
			return entity.find(ModelElementProviderSourceComponent.class)
				.map(component -> component.get().map(ignored1 -> valueSupplier.get()))
				.orElseGet(() -> ProviderUtils.supplied(valueSupplier::get));
		});
		val p = provider;
		val factory = new NamedDomainObjectProviderFactory();
		val propertyStrategy = new ModelBackedGradlePropertyConvertibleStrategy(entity);
		val providerStrategy = new ConfigurableProviderConvertibleStrategy() {
			@Override
			public <S> NamedDomainObjectProvider<S> asProvider(ModelType<S> t) {
				return factory.create(NamedDomainObjectProviderSpec.builder().named(() -> entity.get(FullyQualifiedNameComponent.class).get().toString()).delegateTo(p).typedAs(t.getConcreteType()).configureUsing(action -> configurableStrategy.configure(t, action)).build());
			}
		};
		val identifierSupplier = new IdentifierSupplier(entity, type);
		return new DefaultModelProperty<>(namedStrategy, identifierSupplier, type, providerStrategy, propertyStrategy, configurableStrategy, castableStrategy, propertyLookup, elementLookup, mixInStrategy, valueSupplier, () -> entity);
	}

	private static final class GradlePropertyBackedValueSupplier<T> implements Supplier<T> {
		private final ModelNode entity;

		private GradlePropertyBackedValueSupplier(ModelNode entity) {
			this.entity = entity;
		}

		@Override
		@SuppressWarnings("unchecked")
		public T get() {
			val value = entity.get(GradlePropertyComponent.class).get();
			if (value instanceof Provider) {
				return ((Provider<T>) value).getOrNull();
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
			return entity.find(IdentifierComponent.class).map(IdentifierComponent::get).orElseGet(() -> ModelIdentifier.of(entity.get(ModelPathComponent.class).get(), type));
		}
	}

	private static Stream<ModelType<?>> castableTypes(ModelNode entity) {
		return ModelNodeUtils.getProjections(entity).map(ModelProjection::getType);
	}
}
