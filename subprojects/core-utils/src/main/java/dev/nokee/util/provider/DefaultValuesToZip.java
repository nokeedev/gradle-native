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
package dev.nokee.util.provider;

import java.util.List;

final class DefaultValuesToZip implements ZipProviderBuilder.ValuesToZip {
	private final List<Object> values;

	DefaultValuesToZip(List<Object> values) {
		this.values = values;
	}

	@Override
	public <V> V get(int index) {
		@SuppressWarnings("unchecked")
		final V result = (V) values.get(index);
		return result;
	}

	@Override
	public Object[] values() {
		return values.toArray(new Object[0]);
	}
}
