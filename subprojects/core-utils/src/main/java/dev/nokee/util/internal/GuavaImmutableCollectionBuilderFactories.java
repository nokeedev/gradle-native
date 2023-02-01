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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.utils.Cast;

import static dev.nokee.utils.SupplierUtils.ofSerializableSupplier;

public final class GuavaImmutableCollectionBuilderFactories {
	private static final FlatTransformEachToCollectionAdapter.CollectionBuilderFactory<Object> LIST = new GuavaImmutableCollectionBuilderFactory<>(ofSerializableSupplier(ImmutableList::builder));
	private static final FlatTransformEachToCollectionAdapter.CollectionBuilderFactory<Object> SET = new GuavaImmutableCollectionBuilderFactory<>(ofSerializableSupplier(ImmutableSet::builder));

	private GuavaImmutableCollectionBuilderFactories() {}

	public static <ElementType> FlatTransformEachToCollectionAdapter.CollectionBuilderFactory<ElementType> listFactory() {
		return Cast.uncheckedCast("types already checked by caller", LIST);
	}

	public static <ElementType> FlatTransformEachToCollectionAdapter.CollectionBuilderFactory<ElementType> setFactory() {
		return Cast.uncheckedCast("types already checked by caller", SET);
	}
}
