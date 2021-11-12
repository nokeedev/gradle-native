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

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.core.CoordinateAxis;

import java.util.Comparator;

final class SingleVariantDimensionComparator<T> implements Comparator<Variant> {
	private final CoordinateAxis<T> type;
	private final Comparator<? super T> delegate;

	protected SingleVariantDimensionComparator(CoordinateAxis<T> type, Comparator<? super T> delegate) {
		this.type = type;
		this.delegate = delegate;
	}

	@Override
	public final int compare(Variant lhs, Variant rhs) {
		return compare((BuildVariantInternal) lhs.getBuildVariant(), (BuildVariantInternal) rhs.getBuildVariant());
	}

	private int compare(BuildVariantInternal lhs, BuildVariantInternal rhs) {
		if (lhs.hasAxisValue(type) && rhs.hasAxisValue(type)) {
			return delegate.compare(lhs.getAxisValue(type), rhs.getAxisValue(type));
		} else if (lhs.hasAxisValue(type)) {
			return -1;
		} else if (rhs.hasAxisValue(type)) {
			return 1;
		}
		return 0;
	}
}
