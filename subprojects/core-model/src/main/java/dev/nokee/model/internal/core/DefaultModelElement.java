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

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.ConfigurableStrategy;
import dev.nokee.model.internal.ModelElementFactory;
import dev.nokee.model.internal.NamedStrategy;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;

import java.util.Objects;
import java.util.function.Supplier;

// TODO: implementing ModelNodeAware is simply for legacy reason, it needs to be removed.
public final class DefaultModelElement implements ModelElement, ModelNodeAware {
	private final NamedStrategy namedStrategy;
	private final ConfigurableStrategy configurableStrategy;
	private final ModelCastableStrategy castableStrategy;
	private final ModelPropertyLookupStrategy propertyLookup;
	private final ModelElementLookupStrategy elementLookup;
	private final ModelMixInStrategy mixInStrategy;
	private final Supplier<ModelNode> entitySupplier;

	public DefaultModelElement(NamedStrategy namedStrategy, ConfigurableStrategy configurableStrategy, ModelCastableStrategy castableStrategy, ModelPropertyLookupStrategy propertyLookup, ModelElementLookupStrategy elementLookup, ModelMixInStrategy mixInStrategy) {
		this(namedStrategy, configurableStrategy, castableStrategy, propertyLookup, elementLookup, mixInStrategy, () -> { throw new UnsupportedOperationException(); });
	}

	public DefaultModelElement(NamedStrategy namedStrategy, ConfigurableStrategy configurableStrategy, ModelCastableStrategy castableStrategy, ModelPropertyLookupStrategy propertyLookup, ModelElementLookupStrategy elementLookup, ModelMixInStrategy mixInStrategy, Supplier<ModelNode> entitySupplier) {
		this.namedStrategy = Objects.requireNonNull(namedStrategy);
		this.configurableStrategy = Objects.requireNonNull(configurableStrategy);
		this.castableStrategy = Objects.requireNonNull(castableStrategy);
		this.propertyLookup = Objects.requireNonNull(propertyLookup);
		this.elementLookup = Objects.requireNonNull(elementLookup);
		this.mixInStrategy = Objects.requireNonNull(mixInStrategy);
		this.entitySupplier = Objects.requireNonNull(entitySupplier);
	}

	@Override
	public <S> DomainObjectProvider<S> as(ModelType<S> type) {
		Objects.requireNonNull(type);
		return castableStrategy.castTo(type);
	}

	public <S> S asType(Class<S> ignored) {
		throw new UnsupportedOperationException("Use ModelElement#as(ModelType) instead.");
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
	public <S> DomainObjectProvider<S> element(String name, Class<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return elementLookup.get(name, ModelType.of(type));
	}

	@Override
	public <S> DomainObjectProvider<S> element(String name, ModelType<S> type) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(type);
		return elementLookup.get(name, type);
	}

	@Override
	public <S> ModelElement configure(ModelType<S> type, Action<? super S> action) {
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
	public String getName() {
		return namedStrategy.getAsString();
	}

	@Override
	public ModelNode getNode() {
		return entitySupplier.get();
	}
}
