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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.utils.Cast;
import dev.nokee.utils.TransformerUtils;
import org.gradle.api.Transformer;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class FlatTransformEachAdapter<OutputElementType, InputElementType, OutputType extends Collection<OutputElementType>> implements TransformerUtils.Transformer<OutputType, Iterable<InputElementType>>, Serializable {
	private final CollectionBuilderFactory<OutputElementType> collectionFactory;
	private final org.gradle.api.Transformer<? extends Iterable<OutputElementType>, ? super InputElementType> mapper;

	public FlatTransformEachAdapter(CollectionBuilderFactory<OutputElementType> collectionFactory, org.gradle.api.Transformer<? extends Iterable<OutputElementType>, ? super InputElementType> mapper) {
		this.mapper = requireNonNull(mapper);
		this.collectionFactory = collectionFactory;
	}

	@Override
	public OutputType transform(Iterable<InputElementType> elements) {
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

	private enum CollectionFactories implements CollectionBuilderFactory<Object> {
		LIST {
			@Override
			public CollectionBuilder<Object> create() {
				return new CollectionBuilder<Object>() {
					private final ImmutableList.Builder<Object> builder = ImmutableList.builder();

					@Override
					public CollectionBuilder<Object> addAll(Iterable<Object> elements) {
						builder.addAll(elements);
						return this;
					}

					@Override
					public Collection<Object> build() {
						return builder.build();
					}
				};
			}
		},
		SET {
			@Override
			public CollectionBuilder<Object> create() {
				return new CollectionBuilder<Object>() {
					private final ImmutableSet.Builder<Object> builder = ImmutableSet.builder();

					@Override
					public CollectionBuilder<Object> addAll(Iterable<Object> values) {
						builder.addAll(values);
						return this;
					}

					@Override
					public Collection<Object> build() {
						return builder.build();
					}
				};
			}
		};

		public <T> CollectionBuilderFactory<T> withNarrowTypes() {
			return Cast.uncheckedCast("types already checked by caller", this);
		}
	}

	public static <OUT, IN> FlatTransformEachAdapter<OUT, IN, List<OUT>> flatTransformEachToList(Transformer<? extends Iterable<OUT>, ? super IN> mapper) {
		return new FlatTransformEachAdapter<>(CollectionFactories.LIST.withNarrowTypes(), mapper);
	}

	public static <OUT, IN> FlatTransformEachAdapter<OUT, IN, Set<OUT>> flatTransformEachToSet(Transformer<? extends Iterable<OUT>, ? super IN> mapper) {
		return new FlatTransformEachAdapter<>(CollectionFactories.SET.withNarrowTypes(), mapper);
	}
}
