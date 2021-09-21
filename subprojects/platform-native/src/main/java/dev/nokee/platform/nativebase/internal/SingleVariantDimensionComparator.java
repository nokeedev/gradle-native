/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.core.CoordinateAxis;

import java.util.Comparator;

final class SingleVariantDimensionComparator<T> implements Comparator<VariantInternal> {
	private final CoordinateAxis<T> type;
	private final Comparator<? super T> delegate;

	protected SingleVariantDimensionComparator(CoordinateAxis<T> type, Comparator<? super T> delegate) {
		this.type = type;
		this.delegate = delegate;
	}

	@Override
	public final int compare(VariantInternal lhs, VariantInternal rhs) {
		if (lhs.getBuildVariant().hasAxisValue(type) && rhs.getBuildVariant().hasAxisValue(type)) {
			return delegate.compare(lhs.getBuildVariant().getAxisValue(type), rhs.getBuildVariant().getAxisValue(type));
		} else if (lhs.getBuildVariant().hasAxisValue(type)) {
			return -1;
		} else if (rhs.getBuildVariant().hasAxisValue(type)) {
			return 1;
		}
		return 0;
	}
}
