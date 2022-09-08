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
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import lombok.val;

import java.util.Collections;

public final class ModelBuffers {
	private ModelBuffers() {}

	public static <E extends ModelBufferElement> ModelComponentReference<ModelBufferComponent<E>> referenceOf(Class<E> elementType) {
		return ModelComponentReference.ofInstance(typeOf(elementType));
	}

	public static <T extends ModelBufferElement> ModelComponentType<ModelBufferComponent<T>> typeOf(Class<T> elementType) {
		@SuppressWarnings("unchecked")
		val result = (ModelComponentType<ModelBufferComponent<T>>) ((ModelComponentType<?>) ModelComponentType.componentOf(elementType));
		return result;
	}

	public static <E extends ModelBufferElement> ModelBufferComponent<E> empty(Class<E> elementType) {
		return new ModelBufferComponent<>(elementType, Collections.emptyList());
	}

	@SafeVarargs
	public static <E extends ModelBufferElement> ModelBufferComponent<E> of(E firstElement, E... otherElements) {
		@SuppressWarnings("unchecked")
		val elementType = (Class<E>) firstElement.getClass();
		val builder = ImmutableList.<E>builder().add(firstElement);
		for (E otherElement : otherElements) {
			builder.add(otherElement);
		}
		return new ModelBufferComponent<>(elementType, builder.build());
	}

	public static <E extends ModelBufferElement> ModelBufferComponent<E> of(Class<E> elementType, Iterable<E> elements) {
		val builder = ImmutableList.<E>builder().addAll(elements);
		return new ModelBufferComponent<>(elementType, builder.build());
	}
}
