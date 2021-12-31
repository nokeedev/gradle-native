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

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.ConfigurableProviderConvertibleStrategy;
import dev.nokee.model.internal.ConfigurableStrategy;
import dev.nokee.model.internal.NamedStrategy;
import dev.nokee.model.internal.PropertyConvertibleStrategy;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.HasConfigurableValue;
import org.gradle.api.provider.Provider;

import java.util.Objects;
import java.util.function.Supplier;

// TODO: Remove ModelNodeAware implementation
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class DefaultModelProperty<T> implements ModelProperty<T>, ModelNodeAware {
	private final NamedStrategy namedStrategy;
	private final Supplier<DomainObjectIdentifier> identifierSupplier;
	private final ModelType<T> type;
	private final ConfigurableProviderConvertibleStrategy providerConvertibleStrategy;
	private final PropertyConvertibleStrategy propertyConvertibleStrategy;
	private final ConfigurableStrategy configurableStrategy;
	private final ModelCastableStrategy castableStrategy;
	private final ModelPropertyLookupStrategy propertyLookupStrategy;
	private final ModelElementLookupStrategy elementLookupStrategy;
	private final ModelMixInStrategy mixInStrategy;
	private final Supplier<? extends T> valueSupplier;
	private final Supplier<ModelNode> entitySupplier;

	public DefaultModelProperty(NamedStrategy namedStrategy, Supplier<DomainObjectIdentifier> identifierSupplier, ModelType<T> type, ConfigurableProviderConvertibleStrategy providerConvertibleStrategy, PropertyConvertibleStrategy propertyConvertibleStrategy, ConfigurableStrategy configurableStrategy, ModelCastableStrategy castableStrategy, ModelPropertyLookupStrategy propertyLookupStrategy, ModelElementLookupStrategy elementLookupStrategy, ModelMixInStrategy mixInStrategy, Supplier<? extends T> valueSupplier, Supplier<ModelNode> entitySupplier) {
		this.namedStrategy = Objects.requireNonNull(namedStrategy);
		this.identifierSupplier = Objects.requireNonNull(identifierSupplier);
		this.type = Objects.requireNonNull(type);
		this.providerConvertibleStrategy = Objects.requireNonNull(providerConvertibleStrategy);
		this.propertyConvertibleStrategy = Objects.requireNonNull(propertyConvertibleStrategy);
		this.configurableStrategy = Objects.requireNonNull(configurableStrategy);
		this.castableStrategy = Objects.requireNonNull(castableStrategy);
		this.propertyLookupStrategy = Objects.requireNonNull(propertyLookupStrategy);
		this.elementLookupStrategy = Objects.requireNonNull(elementLookupStrategy);
		this.mixInStrategy = Objects.requireNonNull(mixInStrategy);
		this.valueSupplier = Objects.requireNonNull(valueSupplier);
		this.entitySupplier = Objects.requireNonNull(entitySupplier);
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
	public ModelProperty<T> configure(Action<? super T> action) {
		Objects.requireNonNull(action);
		configurableStrategy.configure(type, action);
		return this;
	}

	@Override
	public ModelProperty<T> configure(@SuppressWarnings("rawtypes") Closure closure) {
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
		throw new UnsupportedOperationException("Use ModelProperty#as(ModelType) instead.");
	}

	@Override
	public boolean instanceOf(ModelType<?> type) {
		Objects.requireNonNull(type);
		return castableStrategy.instanceOf(type);
	}

	@Override
	public ModelElement property(String name) {
		Objects.requireNonNull(name);
		return propertyLookupStrategy.get(name);
	}

	@Override
	public ModelElement element(String name) {
		Objects.requireNonNull(name);
		return elementLookupStrategy.get(name);
	}

	@Override
	public <S> DomainObjectProvider<S> element(String name, Class<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return elementLookupStrategy.get(name, ModelType.of(type));
	}

	@Override
	public <S> DomainObjectProvider<S> element(String name, ModelType<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return elementLookupStrategy.get(name, type);
	}

	@Override
	public <S> ModelProperty<T> configure(ModelType<S> type, Action<? super S> action) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(action);
		configurableStrategy.configure(type, action);
		return this;
	}

	@Override
	public <S> DomainObjectProvider<S> mixin(ModelType<S> type) {
		Objects.requireNonNull(type);
		return mixInStrategy.mixin(type);
	}

	@Override
	public <P extends HasConfigurableValue> P asProperty(ModelType<P> propertyType) {
		Objects.requireNonNull(propertyType);
		return propertyConvertibleStrategy.asProperty(propertyType);
	}

	@Override
	public String getName() {
		return namedStrategy.getAsString();
	}

	@Override
	public ModelNode getNode() {
		return entitySupplier.get();
	}
}
