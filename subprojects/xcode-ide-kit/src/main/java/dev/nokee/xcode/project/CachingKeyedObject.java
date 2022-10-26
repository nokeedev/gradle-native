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
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
public final class CachingKeyedObject implements KeyedObject {
	@EqualsAndHashCode.Exclude private final Map<CodingKey, Object> cache = new HashMap<>();
	private final KeyedObject delegate;

	public CachingKeyedObject(KeyedObject delegate) {
		assert delegate != null : "'delegate' must not be null";
		this.delegate = delegate;
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		assert key != null : "'key' must not be null";
		if (!cache.containsKey(key)) {
			cache.put(key, delegate.tryDecode(key));
		}

		@SuppressWarnings("unchecked")
		final T result = (T) cache.get(key);
		return result;
	}

	@Nullable
	@Override
	public String globalId() {
		return delegate.globalId();
	}

	@Override
	public String isa() {
		return tryDecode(KeyedCoders.ISA);
	}

	@Override
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
