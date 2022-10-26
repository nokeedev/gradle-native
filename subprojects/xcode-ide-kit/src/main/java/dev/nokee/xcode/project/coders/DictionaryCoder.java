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

import dev.nokee.xcode.project.Decoder;
import dev.nokee.xcode.project.Encoder;
import dev.nokee.xcode.project.ValueCoder;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode
public final class DictionaryCoder implements ValueCoder<Map<String, ?>> {
	@Override
	public Map<String, ?> decode(Decoder context) {
		return context.decodeDictionary();
	}

	@Override
	public void encode(Map<String, ?> value, Encoder context) {
		context.encodeDictionary(value);
	}
}
