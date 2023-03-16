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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

import javax.annotation.Nullable;

public final class IsaKeyedObject implements KeyedObject {
	private final long age = System.nanoTime();
	private final String isa;

	public IsaKeyedObject(String isa) {
		assert isa != null;
		assert !isa.isEmpty();
		this.isa = isa;
	}

	@Override
	public String isa() {
		return isa;
	}

	@Nullable
	@Override
	public String globalId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long age() {
		return age;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T tryDecode(CodingKey key) {
		if (KeyedCoders.ISA.equals(key)) {
			return (T) isa;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void encode(EncodeContext context) {
		throw new UnsupportedOperationException();
	}
}
