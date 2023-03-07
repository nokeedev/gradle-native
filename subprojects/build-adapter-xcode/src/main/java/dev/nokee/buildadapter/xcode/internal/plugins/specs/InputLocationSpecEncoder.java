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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class InputLocationSpecEncoder<IN> implements ValueEncoder<XCBuildSpec, IN> {
	private final ValueEncoder<PBXReference, IN> delegate;

	public InputLocationSpecEncoder(ValueEncoder<PBXReference, IN> delegate) {
		this.delegate = delegate;
	}

	@Override
	public XCBuildSpec encode(IN value, Context context) {
		return new InputLocationSpec(delegate.encode(value, context));
	}
}
