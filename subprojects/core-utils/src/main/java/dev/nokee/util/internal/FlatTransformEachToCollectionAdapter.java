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
package dev.nokee.util.internal;

import lombok.EqualsAndHashCode;
import org.gradle.api.Transformer;

import java.io.Serializable;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class FlatTransformEachToCollectionAdapter<OutputType extends Collection<OutputElementType>, OutputElementType, InputElementType> implements Transformer<OutputType, Iterable<? extends InputElementType>>, Serializable {
	private final CollectionBuilderFactory<OutputElementType> collectionFactory;
	private final Transformer<? extends Iterable<OutputElementType>, ? super InputElementType> mapper;

	public FlatTransformEachToCollectionAdapter(CollectionBuilderFactory<OutputElementType> collectionFactory, Transformer<? extends Iterable<OutputElementType>, InputElementType> mapper) {
		this.mapper = requireNonNull(mapper);
		this.collectionFactory = collectionFactory;
	}

	@Override
	public OutputType transform(Iterable<? extends InputElementType> elements) {
		CollectionBuilder<OutputElementType> builder = collectionFactory.create();
		for (InputElementType element : elements) {
			builder.addAll(mapper.transform(element));
		}

		@SuppressWarnings("unchecked")
		final OutputType result = (OutputType) builder.build();
		return result;
	}

	@Override
	public String toString() {
		return "TransformerUtils.flatTransformEach(" + mapper + ")";
	}

	public interface CollectionBuilderFactory<T> {
		CollectionBuilder<T> create();
	}

	public interface CollectionBuilder<T> {
		CollectionBuilder<T> addAll(Iterable<T> values);
		Collection<T> build();
	}
}
