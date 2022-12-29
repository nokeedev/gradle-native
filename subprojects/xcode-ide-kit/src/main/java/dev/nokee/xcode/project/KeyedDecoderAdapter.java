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

import dev.nokee.xcode.project.coders.DictionaryDecoder;
import dev.nokee.xcode.project.coders.FalseTrueBooleanDecoder;
import dev.nokee.xcode.project.coders.IntegerDecoder;
import dev.nokee.xcode.project.coders.ListDecoder;
import dev.nokee.xcode.project.coders.NoYesBooleanDecoder;
import dev.nokee.xcode.project.coders.ObjectDecoder;
import dev.nokee.xcode.project.coders.StringDecoder;
import dev.nokee.xcode.project.coders.ThrowingValueDecoder;
import dev.nokee.xcode.project.coders.ZeroOneBooleanDecoder;

import java.util.List;
import java.util.Map;

public abstract class KeyedDecoderAdapter implements KeyedDecoder {
	protected abstract <T> T tryDecode(String key, ValueDecoder<T, Object> decoder);

	@Override
	public final <T> T decode(String key, ValueDecoder<T, Object> decoder) {
		return tryDecode(key, decoder);
	}

	@Override
	public final <T extends Codeable> T decodeObject(String key, ValueDecoder<T, KeyedObject> factory) {
		return tryDecode(key, new ObjectDecoder<>(factory));
	}

	@Override
	public final Boolean decodeBoolean(String key) {
		return tryDecode(key, new ZeroOneBooleanDecoder(new NoYesBooleanDecoder(new FalseTrueBooleanDecoder())));
	}

	@Override
	public final Map<String, ?> decodeDictionary(String key) {
		return tryDecode(key, DictionaryDecoder.newDictionaryDecoder());
	}

	@Override
	public final String decodeString(String key) {
		return tryDecode(key, StringDecoder.newStringDecoder());
	}

	@Override
	public final Integer decodeInteger(String key) {
		return tryDecode(key, IntegerDecoder.newIntegerDecoder());
	}

	@Override
	public final <T> List<T> decodeArray(String key, ValueDecoder<T, Object> decoder) {
		return tryDecode(key, new ListDecoder<>(decoder));
	}
}
