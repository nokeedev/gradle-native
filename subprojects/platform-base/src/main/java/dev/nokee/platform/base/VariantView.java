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
package dev.nokee.platform.base;

/**
 * A view of the variants that are created and configured as they are required.
 *
 * @param <T> type of the elements in this view
 * @since 0.2
 */
public interface VariantView<T extends Variant> extends View<T> {
	/**
	 * Returns a variant view containing the objects in this view of the given type.
	 * The returned collection is live, so that when matching objects are later added to this view, they are also visible in the filtered variant view.
	 *
	 * @param type The type of variant to find.
	 * @param <S> The base type of the new variant view.
	 * @return the matching element as a {@link VariantView}, never null.
	 */
	<S extends T> VariantView<S> withType(Class<S> type);
}
