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

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;

public final class PBXObjectFields {
	private final ImmutableMap<String, Object> fields;

	private PBXObjectFields(ImmutableMap<String, Object> fields) {
		this.fields = fields;
	}

	public static PBXObjectFields fromMap(Map<String, Object> fields) {
		return new PBXObjectFields(ImmutableMap.copyOf(fields));
	}

	public Object get(String name) {
		return fields.get(name);
	}

	public int size() {
		return fields.size();
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return fields.entrySet();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

		public Builder putField(String name, Object value) {
			builder.put(name, value);
			return this;
		}

		public PBXObjectFields build() {
			return new PBXObjectFields(builder.build());
		}
	}
}
