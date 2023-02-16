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
package dev.nokee.util.internal;

import dev.nokee.util.Executable;
import dev.nokee.util.Unpacker;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;

@EqualsAndHashCode
public final class UnpackerExecutableAdapter<T> implements Executable<T> {
	private final Unpacker unpacker;

	public UnpackerExecutableAdapter(Unpacker unpacker) {
		this.unpacker = unpacker;
	}

	@Override
	public T execute(@Nullable Object obj) {
		@SuppressWarnings("unchecked") final T result = (T) unpacker.unpack(obj);
		return result;
	}
}
