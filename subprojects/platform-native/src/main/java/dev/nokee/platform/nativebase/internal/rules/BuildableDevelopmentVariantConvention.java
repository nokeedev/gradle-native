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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.val;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class BuildableDevelopmentVariantConvention<T extends VariantInternal> implements Callable<T> {
	private final DevelopmentVariantConvention<T> delegate;

	public BuildableDevelopmentVariantConvention(Supplier<Iterable<T>> variants) {
		this.delegate = new DevelopmentVariantConvention<>(variants);
	}

	@Override
	public T call() throws Exception {
		val result = delegate.call();
		if (isBuildable(result)) {
			return result;
		}
		return null;
	}

	public boolean isBuildable(T variant) {
		return variant.getBuildVariant().hasAxisOf(Coordinate.of(OperatingSystemFamily.OPERATING_SYSTEM_COORDINATE_AXIS, OperatingSystemFamily.forName(System.getProperty("os.name"))));
	}
}
