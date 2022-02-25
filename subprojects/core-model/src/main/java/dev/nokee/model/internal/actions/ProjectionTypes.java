/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.actions;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.type.ModelType;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

final class ProjectionTypes implements Iterable<ModelType<?>> {
	private final Set<ModelType<?>> values;

	public ProjectionTypes(Set<ModelType<?>> values) {
		this.values = values;
	}

	public ProjectionTypes plus(ModelType<?> element) {
		return new ProjectionTypes(ImmutableSet.<ModelType<?>>builder().addAll(values).add(element).build());
	}

	public Stream<ModelType<?>> stream() {
		return values.stream();
	}

	@Override
	public Iterator<ModelType<?>> iterator() {
		return values.iterator();
	}

	public static ProjectionTypes of(ModelType<?> first, ModelType<?>... others) {
		return new ProjectionTypes(ImmutableSet.<ModelType<?>>builder().add(first).add(others).build());
	}
}
