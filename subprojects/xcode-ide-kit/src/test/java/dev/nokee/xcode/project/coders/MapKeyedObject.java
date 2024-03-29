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
import dev.nokee.xcode.project.KeyedObject;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode
public final class MapKeyedObject implements KeyedObject {
	@EqualsAndHashCode.Exclude private final long age = System.nanoTime();
	private final Map<String, ?> values;

	public MapKeyedObject(Map<String, ?> values) {
		this.values = values;
	}

	@Override
	public String isa() {
		return null;
	}

	@Nullable
	@Override
	public String globalId() {
		return null;
	}

	@Override
	public Map<CodingKey, Object> getAsMap() {
		return values.entrySet().stream().collect(Collectors.toMap(it -> (CodingKey) it::getKey, Map.Entry::getValue));
	}

	@Override
	public long age() {
		return age;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T tryDecode(CodingKey key) {
		return (T) values.get(key.getName());
	}

	@Override
	public void encode(EncodeContext context) {
		throw new UnsupportedOperationException();
	}
}
