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
package dev.nokee.xcode;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public final class XCCacheLoader<T, R extends XCReference> implements XCLoader<T, R>, Serializable {
	private final XCLoader<T, R> delegate;

	public XCCacheLoader(XCLoader<T, R> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T load(R reference) {
		return XCCache.cacheIfAbsent(new CacheKey<>(delegate.getClass(), reference), __ -> delegate.load(reference));
	}

	@EqualsAndHashCode
	private static final class CacheKey<R> {
		private final Class<?> loaderType;
		private final R reference;

		private CacheKey(Class<?> loaderType, R reference) {
			this.loaderType = loaderType;
			this.reference = reference;
		}

		@Override
		public String toString() {
			return loaderType.getSimpleName() + " - " + reference;
		}
	}
}
