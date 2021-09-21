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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.internal.VariantInternal;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static dev.nokee.platform.nativebase.internal.NativeVariantComparators.*;

public class DevelopmentVariantConvention<T extends VariantInternal> implements Callable<T> {
	private final Supplier<Iterable<T>> variants;

	public DevelopmentVariantConvention(Supplier<Iterable<T>> variants) {
		this.variants = variants;
	}

	@Override
	public T call() throws Exception {
		return StreamSupport.stream(this.variants.get().spliterator(), false).min(preferHostOperatingSystemFamily().thenComparing(preferHostMachineArchitecture()).thenComparing(preferSharedBinaryLinkage()).thenComparing(preferDebugBuildType())).orElseThrow(() -> new Exception("No variants available."));
	}
}
