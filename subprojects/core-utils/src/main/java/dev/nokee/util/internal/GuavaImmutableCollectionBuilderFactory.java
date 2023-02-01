/*
 * Copyright 2023 the original author or authors.
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

import com.google.common.collect.ImmutableCollection;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Supplier;

@EqualsAndHashCode
public class GuavaImmutableCollectionBuilderFactory<ElementType> implements FlatTransformEachToCollectionAdapter.CollectionBuilderFactory<ElementType>, Serializable {
	private final Supplier<ImmutableCollection.Builder<ElementType>> builderSupplier;

	public GuavaImmutableCollectionBuilderFactory(Supplier<ImmutableCollection.Builder<ElementType>> builderSupplier) {
		this.builderSupplier = builderSupplier;
	}

	@Override
	public FlatTransformEachToCollectionAdapter.CollectionBuilder<ElementType> create() {
		return new FlatTransformEachToCollectionAdapter.CollectionBuilder<ElementType>() {
			private final ImmutableCollection.Builder<ElementType> builder = builderSupplier.get();

			@Override
			public FlatTransformEachToCollectionAdapter.CollectionBuilder<ElementType> addAll(Iterable<ElementType> values) {
				builder.addAll(values);
				return this;
			}

			@Override
			public Collection<ElementType> build() {
				return builder.build();
			}
		};
	}
}
