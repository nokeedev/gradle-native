/*
 * Copyright 2020 the original author or authors.
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

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@EqualsAndHashCode
public abstract class TypeCompatibilityModelProjectionSupport<M> implements ModelProjection {
	private final ModelType<M> type;

	protected TypeCompatibilityModelProjectionSupport(ModelType<M> type) {
		this.type = Objects.requireNonNull(type);
	}

	public ModelType<M> getType() {
		return type;
	}

	@Override
	public <T> boolean canBeViewedAs(ModelType<T> type) {
		return type.isAssignableFrom(this.type);
	}

	@Override
	public Iterable<String> getTypeDescriptions() {
		return ImmutableList.of(description(type));
	}

	public static String description(ModelType<?> type) {
		if (!type.getSupertype().isPresent() && type.getInterfaces().isEmpty()) {
			return type.toString();
		}
		return type.toString() + " (or assignment compatible type thereof)";
	}
}
