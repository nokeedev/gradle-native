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
package dev.nokee.model.internal.buffers;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelComponentType;

import java.util.Iterator;

public final class ModelBufferComponent<E extends ModelBufferElement> implements ModelComponent, Iterable<E> {
	private final Class<E> elementType;
	private final Iterable<E> elements;

	ModelBufferComponent(Class<E> elementType, Iterable<E> elements) {
		this.elementType = elementType;
		this.elements = elements;
	}

	public ModelBufferComponent<E> appended(E element) {
		return new ModelBufferComponent<>(elementType, ImmutableList.<E>builder().addAll(elements).add(element).build());
	}

	@Override
	public ModelComponentType<?> getComponentType() {
		return ModelBuffers.typeOf(elementType);
	}

	@Override
	public Iterator<E> iterator() {
		return elements.iterator();
	}

	@Override
	public String toString() {
		return "buffer of " + elementType.getSimpleName() + " " + elements;
	}
}
