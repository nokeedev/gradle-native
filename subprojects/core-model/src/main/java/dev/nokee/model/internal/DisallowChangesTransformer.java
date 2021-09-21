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
package dev.nokee.model.internal;

import org.gradle.api.Transformer;

public final class DisallowChangesTransformer<T> implements Transformer<T, T> {
	private boolean changesDisallowed = false;

	@Override
	public T transform(T t) {
		if (changesDisallowed) {
			return t;
		}
		throw new IllegalStateException("Please disallow changes before realizing this collection.");
	}

	public void disallowChanges() {
		changesDisallowed = true;
	}

	public void assertChangesAllowed() {
		if (changesDisallowed) {
			throw new IllegalStateException("The value cannot be changed any further.");
		}
	}
}
