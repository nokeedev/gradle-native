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

import dev.nokee.xcode.project.coders.ZeroOneBooleanCoder;
import dev.nokee.xcode.project.coders.DictionaryCoder;
import dev.nokee.xcode.project.coders.IntegerCoder;
import dev.nokee.xcode.project.coders.ListCoder;
import dev.nokee.xcode.project.coders.ObjectCoder;
import dev.nokee.xcode.project.coders.StringCoder;

import java.util.List;
import java.util.Map;

public abstract class KeyedDecoderAdapter implements KeyedDecoder {
	protected abstract <T> T tryDecode(String key, ValueCoder<T> decoder);

	@Override
	public final <T> T decode(String key, ValueCoder<T> decoder) {
		return tryDecode(key, decoder);
	}

	@Override
	public final <T extends Codeable> T decodeObject(String key, CodeableObjectFactory<T> factory) {
		return tryDecode(key, new ObjectCoder<>(factory));
	}

	@Override
	public final Boolean decodeBoolean(String key) {
		return tryDecode(key, new ZeroOneBooleanCoder());
	}

	@Override
	public final Map<String, ?> decodeDictionary(String key) {
		return tryDecode(key, new DictionaryCoder());
	}

	@Override
	public final String decodeString(String key) {
		return tryDecode(key, new StringCoder());
	}

	@Override
	public final Integer decodeInteger(String key) {
		return tryDecode(key, new IntegerCoder());
	}

	@Override
	public final <T> List<T> decodeArray(String key, ValueCoder<T> decoder) {
		return tryDecode(key, new ListCoder<>(decoder));
	}
}
