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

import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Predicates that matches only on the other axis value.
 * Returning {@literal false} will discard the build variant of the specified values.
 * The intended truth table goes as follows:
 *
 * <table border="1">
 *   <tr>
 *     <th>currentValue vs otherValue</th>
 *     <th>absent</th>
 *     <th>present</th>
 *   </tr>
 *   <tr>
 *     <th>otherAxisValue</th>
 *     <td>0</td>
 *     <td>1</td>
 *   </tr>
 *   <tr>
 *     <th>not otherAxisValue</th>
 *     <td>1</td>
 *     <td>0</td>
 *   </tr>
 * </table>
 *
 * @param <T>  current axis type
 * @param <S>  other axis type
 */
final class VariantDimensionAxisOnlyOnPredicate<T, S> implements BiPredicate<Optional<T>, S> {
	private final Object otherAxisValue;

	VariantDimensionAxisOnlyOnPredicate(Object otherAxisValue) {
		this.otherAxisValue = otherAxisValue;
	}

	@Override
	public boolean test(Optional<T> currentValue, S otherValue) {
		return (!currentValue.isPresent() && !otherValue.equals(otherAxisValue))
			|| (currentValue.isPresent() && otherValue.equals(otherAxisValue));
	}
}
