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

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

import static dev.nokee.platform.base.internal.DevelopmentBinaryUtils.selectSingleBinaryByType;

public enum NativeDevelopmentBinaryConvention implements Transformer<Provider<? extends Binary>, Iterable<? extends Binary>> {
	EXECUTABLE(ExecutableBinary.class),
	SHARED(SharedLibraryBinary.class),
	STATIC(StaticLibraryBinary.class);

	private final Class<? extends Binary> binaryTypeToSelect;

	NativeDevelopmentBinaryConvention(Class<? extends Binary> binaryTypeToSelect) {
		this.binaryTypeToSelect = binaryTypeToSelect;
	}

	@Override
	public Provider<? extends Binary> transform(Iterable<? extends Binary> binaries) {
		return selectSingleBinaryByType(binaryTypeToSelect, binaries);
	}

	public static NativeDevelopmentBinaryConvention of(BinaryLinkage linkage) {
		if (linkage.isExecutable()) {
			return EXECUTABLE;
		} else if (linkage.isShared()) {
			return SHARED;
		} else if (linkage.isStatic()) {
			return STATIC;
		}
		throw new IllegalArgumentException(String.format("Unsupported binary linkage '%s' for native development binary convention", linkage.getName()));
	}
}
