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

import com.google.common.collect.ImmutableMap;

import java.util.function.BiConsumer;

public final class XCBuildSettings {
	private ImmutableMap<String, String> values;

	public XCBuildSettings(ImmutableMap<String, String> values) {
		this.values = values;
	}

	public String get(String key) {
		return values.get(key);
	}

	public void forEach(BiConsumer<? super String, ? super String> action) {
		values.forEach(action);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

		public Builder put(String key, String value) {
			builder.put(key, value);
			return this;
		}

		public XCBuildSettings build() {
			return new XCBuildSettings(builder.build());
		}
	}
}
