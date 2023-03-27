/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class NormalizeStringAsPBXReferenceEncoder<T> implements ValueEncoder<PBXReference, T> {
	private final ValueEncoder<PBXReference, T> delegate;

	public NormalizeStringAsPBXReferenceEncoder(ValueEncoder<PBXReference, T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public PBXReference encode(T value, Context context) {
		if (value instanceof String) {
			return PBXFileReference.ofAbsolutePath((String) value);
		} else {
			return delegate.encode(value, context);
		}
	}
}
