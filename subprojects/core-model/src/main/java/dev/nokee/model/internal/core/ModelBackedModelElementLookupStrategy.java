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
import dev.nokee.model.internal.ElementNameComponent;
import dev.nokee.model.internal.ModelElementFactory;
import dev.nokee.model.internal.type.ModelType;

public final class ModelBackedModelElementLookupStrategy implements ModelElementLookupStrategy {
	private final ModelNode entity;
	private final ModelElementFactory elementFactory;

	public ModelBackedModelElementLookupStrategy(ModelNode entity, ModelElementFactory elementFactory) {
		this.entity = entity;
		this.elementFactory = elementFactory;
	}

	@Override
	public ModelElement get(String name) {
		assert name != null;
		return elementFactory.createElement(ModelNodeUtils.getDirectDescendants(entity).stream().filter(it -> it.getComponent(ElementNameComponent.class).get().toString().equals(name)).collect(MoreCollectors.onlyElement()));
	}

	@Override
	public <S> DomainObjectProvider<S> get(String name, ModelType<S> type) {
		assert name != null;
		assert type != null;
		return elementFactory.createObject(ModelNodeUtils.getDirectDescendants(entity).stream().filter(it -> it.getComponent(ElementNameComponent.class).get().toString().equals(name) && ModelNodeUtils.canBeViewedAs(it, type)).collect(MoreCollectors.onlyElement()), type);
	}
}
