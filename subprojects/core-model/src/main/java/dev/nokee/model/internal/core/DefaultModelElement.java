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
package dev.nokee.model.internal.core;

import com.google.common.collect.MoreCollectors;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.ConfigurableStrategy;
import dev.nokee.model.internal.InstanceOfOperatorStrategy;
import dev.nokee.model.internal.TypeCastOperatorStrategy;
import dev.nokee.model.internal.registry.ModelNodeBackedProvider;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: implementing ModelNodeAware is simply for legacy reason, it needs to be removed.
public final class DefaultModelElement implements ModelElement, ModelNodeAware {
	private final Supplier<String> nameSupplier;
	private final Supplier<String> displayNameSupplier;
	private final ConfigurableStrategy configurableStrategy;
	private final InstanceOfOperatorStrategy instanceOfStrategy;
	private final TypeCastOperatorStrategy typeCastStrategy;
	private final ModelPropertyLookupStrategy propertyLookup;
	private final Supplier<ModelNode> entitySupplier;

	public DefaultModelElement(Supplier<String> nameSupplier, Supplier<String> displayNameSupplier, ConfigurableStrategy configurableStrategy, InstanceOfOperatorStrategy instanceOfStrategy, TypeCastOperatorStrategy typeCastStrategy, ModelPropertyLookupStrategy propertyLookup) {
		this(nameSupplier, displayNameSupplier, configurableStrategy, instanceOfStrategy, typeCastStrategy, propertyLookup, () -> { throw new UnsupportedOperationException(); });
	}

	private DefaultModelElement(Supplier<String> nameSupplier, Supplier<String> displayNameSupplier, ConfigurableStrategy configurableStrategy, InstanceOfOperatorStrategy instanceOfStrategy, TypeCastOperatorStrategy typeCastStrategy, ModelPropertyLookupStrategy propertyLookup, Supplier<ModelNode> entitySupplier) {
		this.nameSupplier = Objects.requireNonNull(nameSupplier);
		this.displayNameSupplier = Objects.requireNonNull(displayNameSupplier);
		this.configurableStrategy = Objects.requireNonNull(configurableStrategy);
		this.instanceOfStrategy = Objects.requireNonNull(instanceOfStrategy);
		this.typeCastStrategy = Objects.requireNonNull(typeCastStrategy);
		this.propertyLookup = Objects.requireNonNull(propertyLookup);
		this.entitySupplier = Objects.requireNonNull(entitySupplier);
	}

	public static DefaultModelElement of(ModelNode entity) {
		Objects.requireNonNull(entity);
		val nameSupplier = entity.getComponent(ElementNameComponent.class);
		val displayNameSupplier = entity.getComponent(DisplayNameComponent.class);
		val instanceOfStrategy = new InstanceOfOperatorStrategy() {
			@Override
			public boolean instanceOf(ModelType<?> type) {
				assert type != null;
				return ModelNodeUtils.canBeViewedAs(entity, type);
			}

			@Override
			public Stream<ModelType<?>> getCastableTypes() {
				return ModelNodeUtils.getProjections(entity).map(it -> it.getType());
			}
		};
		val typeCastStrategy = new TypeCastOperatorStrategy() {
			@Override
			public <T> DomainObjectProvider<T> castTo(ModelType<T> type) {
				assert type != null;
				val result = ModelNodeUtils.getProjections(entity).filter(it -> it.canBeViewedAs(type)).collect(MoreCollectors.toOptional());
				assert result.isPresent();
				return new ModelNodeBackedProvider<>((ModelType<T>) result.get().getType(), entity);
			}
		};
		val configurableStrategy = new ConfigurableStrategy() {
			@Override
			public <S> void configure(ModelType<S> type, Action<? super S> action) {
				assert type != null;
				assert action != null;
				if (type.isSubtypeOf(Property.class)) {
					action.execute(typeCastStrategy.castTo(type).get());
				} else {
					typeCastStrategy.castTo(type).configure(action);
				}
			}
		};
		val propertyLookup = new ModelPropertyLookupStrategy() {
			@Override
			public ModelElement get(String propertyName) {
				assert propertyName != null;
				return ModelProperties.getProperty(entity, propertyName);
			}
		};
		return new DefaultModelElement(
			nameSupplier,
			displayNameSupplier,
			configurableStrategy,
			instanceOfStrategy,
			typeCastStrategy,
			propertyLookup,
			() -> entity
		);
	}

	@Override
	public <S> DomainObjectProvider<S> as(ModelType<S> type) {
		Objects.requireNonNull(type);
		if (!instanceOf(type)) {
			throw new ClassCastException(String.format("Could not cast %s to %s. Available instances: %s.", getDisplayName(), type.getConcreteType().getSimpleName(), instanceOfStrategy.getCastableTypes().map(it -> it.getConcreteType().getSimpleName()).collect(Collectors.joining(", "))));
		}
		return typeCastStrategy.castTo(type);
	}

	public <S> S asType(Class<S> ignored) {
		throw new UnsupportedOperationException("Use ModelElement#as(ModelType) instead.");
	}

	@Override
	public boolean instanceOf(ModelType<?> type) {
		Objects.requireNonNull(type);
		return instanceOfStrategy.instanceOf(type);
	}

	@Override
	public ModelElement property(String name) {
		Objects.requireNonNull(name);
		return propertyLookup.get(name);
	}

	@Override
	public <S> ModelElement configure(ModelType<S> type, Action<? super S> action) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(action);
		if (!instanceOf(type)) {
			throw new RuntimeException("...");
		}
		configurableStrategy.configure(type, action);
		return this;
	}

	@Override
	public String getName() {
		return nameSupplier.get();
	}

	public String getDisplayName() {
		return displayNameSupplier.get();
	}

	@Override
	public ModelNode getNode() {
		return entitySupplier.get();
	}
}
