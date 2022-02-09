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

import dev.nokee.provider.ProviderConvertible;
import dev.nokee.runtime.core.CoordinateSet;
import org.gradle.api.provider.Provider;

public final class VariantDimensionValuesComponent implements ProviderConvertible<CoordinateSet<?>> {
	private final Provider<CoordinateSet<?>> value;

	public VariantDimensionValuesComponent(Provider<CoordinateSet<?>> value) {
		this.value = value;
	}

	public CoordinateSet<?> get() {
		return value.get();
	}

	@Override
	public Provider<CoordinateSet<?>> asProvider() {
		return value;
	}
}
