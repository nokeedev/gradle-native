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
package dev.nokee.xcode.project;

import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Set;

@EqualsAndHashCode
public final class RecodeableKeyedObject implements KeyedObject {
	private final KeyedObject delegate;
	private final Set<CodingKey> knownKeys;

	public RecodeableKeyedObject(KeyedObject delegate, Set<CodingKey> knownKeys) {
		this.delegate = delegate;
		this.knownKeys = knownKeys;
	}

	@Override
	public String isa() {
		return delegate.isa();
	}

	@Nullable
	@Override
	public String globalId() {
		return delegate.globalId();
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	@Override
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}
}
