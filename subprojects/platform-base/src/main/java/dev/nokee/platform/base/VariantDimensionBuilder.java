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
package dev.nokee.platform.base;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * A builder for configuring the new dimension participating in the variant calculation of a {@link VariantAwareComponent} instance.
 *
 * @since 0.5
 */
public interface VariantDimensionBuilder<T> {
	/**
	 * Includes this new variant dimension values only on the specified other axis value.
	 *
	 * @param otherAxisValue  the other axis value to include the values of this dimension, must not be null
	 * @return this builder, never null
	 */
	VariantDimensionBuilder<T> onlyOn(Object otherAxisValue);

	/**
	 * Excludes this new variant dimension values for the specified other axis value.
	 *
	 * @param otherAxisValue  the other axis value to exclude the values of this dimension, must not be null
	 * @return this builder, never null
	 */
	VariantDimensionBuilder<T> exceptOn(Object otherAxisValue);

	/**
	 * Includes this new variant dimension values only if the specified predicate returns true for other axis values.
	 * Note that all variants without the other axis are implicitly included.
	 *
	 * @param otherAxisType  the other axis type, must not be null
	 * @param predicate  the only-if predicate, must not be null
	 * @param <S>  the type of the other axis
	 * @return this builder, never null
	 */
	<S> VariantDimensionBuilder<T> onlyIf(Class<S> otherAxisType, BiPredicate<? super Optional<T>, ? super S> predicate);

	/**
	 * Exclude this new variant dimension values only if the specified predicate returns true for other axis values.
	 * Note that all variants without the other axis are implicitly included.
	 *
	 * @param otherAxisType  the other axis type, must not be null
	 * @param predicate  the except-if predicate, must not be null
	 * @param <S>  the type of the other axis
	 * @return this builder, never null
	 */
	<S> VariantDimensionBuilder<T> exceptIf(Class<S> otherAxisType, BiPredicate<? super Optional<T>, ? super S> predicate);
}
