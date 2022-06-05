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
package dev.nokee.xcode.objects.configuration;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.Map;

@EqualsAndHashCode
public final class BuildSettings {
	private static final BuildSettings EMPTY_BUILD_SETTINGS = new BuildSettings(ImmutableMap.of());
	private final ImmutableMap<String, Object> values;

	private BuildSettings(ImmutableMap<String, Object> values) {
		this.values = values;
	}

	public static BuildSettings of(Map<String, Object> values) {
		return new BuildSettings(ImmutableMap.copyOf(values));
	}

    public int size() {
		return values.size();
	}

	public Map<String, ?> asMap() {
		return values;
	}

	public static BuildSettings empty() {
		return EMPTY_BUILD_SETTINGS;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final Map<String, Object> values = new LinkedHashMap<>();

		public Builder put(String key, Object value) {
			values.put(key, value);
			return this;
		}

		public BuildSettings build() {
			return new BuildSettings(ImmutableMap.copyOf(values));
		}
	}
}
