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
package dev.nokee.platform.base.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import lombok.val;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static dev.nokee.runtime.core.Coordinates.isAbsentCoordinate;
import static java.util.stream.Collectors.groupingBy;

final class VariantDimensionAxisFilter<T, S> implements Predicate<BuildVariantInternal> {
	private final CoordinateAxis<T> axis;
	private final Class<S> otherAxisType;
	private final BiPredicate<? super Optional<T>, ? super S> predicate;

	public VariantDimensionAxisFilter(CoordinateAxis<T> axis, Class<S> otherAxisType, BiPredicate<? super Optional<T>, ? super S> predicate) {
		this.axis = axis;
		this.otherAxisType = otherAxisType;
		this.predicate = predicate;
	}

	@Override
	public boolean test(BuildVariantInternal buildVariant) {
		val coordinates = buildVariant.getDimensions().stream().collect(groupingBy(it -> it.getAxis().getType(), onlyElement()));

		@SuppressWarnings("unchecked")
		val otherCoordinate = (Coordinate<S>) coordinates.get(otherAxisType);
		if (otherCoordinate == null) {
			return true;
		} else if (isAbsentCoordinate(otherCoordinate)) {
			return true;
		}

		@SuppressWarnings("unchecked")
		val currentCoordinate = (Coordinate<T>) coordinates.get(axis.getType());
		assert currentCoordinate != null : "variant dimension filter only works when current axis is present";
		if (isAbsentCoordinate(currentCoordinate)) {
			return predicate.test(Optional.empty(), otherCoordinate.getValue());
		} else {
			return predicate.test(Optional.of(currentCoordinate.getValue()), otherCoordinate.getValue());
		}
	}
}
