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
package dev.nokee.model.internal.registry;

import com.google.common.collect.MoreCollectors;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.Action;

import java.lang.reflect.Type;
import java.util.Objects;

public final class ModelNodeBackedElement implements ModelElement, ModelNodeAware {
	private final ModelNode node;

	public ModelNodeBackedElement(ModelNode node) {
		this.node = node;
	}

	@Override
	public String getName() {
		return ModelNodeUtils.getPath(node).getName();
	}

	@Override
	public <T> DomainObjectProvider<T> as(ModelType<T> type) {
		Objects.requireNonNull(type);
		val result = ModelNodeUtils.getProjections(node).filter(it -> it.canBeViewedAs(type)).collect(MoreCollectors.toOptional()).orElseThrow(() -> new ClassCastException());
		return new ModelNodeBackedProvider<>((ModelType<T>) result.getType(), node);
	}

	public <T> T asType(Class<T> type) {
		if (DomainObjectProvider.class.isAssignableFrom(type)) {
			return type.cast(this);
		} else {
			throw new UnsupportedOperationException("Use 'DomainObjectProvider#as(type)' instead.");
		}
	}

	@Override
	public boolean instanceOf(ModelType<?> type) {
		Objects.requireNonNull(type);
		return ModelNodeUtils.canBeViewedAs(node, type);
	}

	@Override
	public <T> ModelElement configure(Class<T> type, Action<? super T> action) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(action);
		as(type).configure(action);
		return this;
	}

	@Override
	public ModelNode getNode() {
		return node;
	}
}
