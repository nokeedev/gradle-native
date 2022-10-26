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

import dev.nokee.xcode.project.CodeableObjectFactory;
import dev.nokee.xcode.project.KeyedCoder;
import dev.nokee.xcode.project.ValueCoder;
import org.hamcrest.Matcher;
import org.junit.platform.commons.util.ReflectionUtils;

import static org.hamcrest.Matchers.equalTo;

public class KeyedCoderMatcherBuilder {
	private final String key;

	public KeyedCoderMatcherBuilder(String key) {
		this.key = key;
	}

	public static KeyedCoderMatcherBuilder ofKey(String key) {
		return new KeyedCoderMatcherBuilder(key);
	}

	public Matcher<KeyedCoder<?>> asString() {
		return equalTo(new FieldCoder<>(key, new StringCoder()));
	}

	public <T> Matcher<KeyedCoder<?>> as(Class<? extends ValueCoder<T>> coderType) {
		return equalTo(new FieldCoder<>(key, ReflectionUtils.newInstance(coderType)));
	}

	public <T extends CodeableObjectFactory<?>> Matcher<KeyedCoder<?>> asListOfRefObject(Class<T> factoryType) {
		return equalTo(new FieldCoder<>(key, new ListCoder<>(new ObjectRefCoder<>((CodeableObjectFactory<?>) ReflectionUtils.newInstance(factoryType)))));
	}

	public <T extends CodeableObjectFactory<?>> Matcher<KeyedCoder<?>> asListOfObject(Class<T> factoryType) {
		return equalTo(new FieldCoder<>(key, new ListCoder<>(new ObjectCoder<>((CodeableObjectFactory<?>) ReflectionUtils.newInstance(factoryType)))));
	}

	public <T extends CodeableObjectFactory<?>> Matcher<KeyedCoder<?>> asRefObject(Class<? extends T> factoryType) {
		return equalTo(new FieldCoder<>(key, new ObjectRefCoder<>((CodeableObjectFactory<?>) ReflectionUtils.newInstance(factoryType))));
	}

	public Matcher<KeyedCoder<?>> asDictionary() {
		return equalTo(new FieldCoder<>(key, new DictionaryCoder()));
	}

	public Matcher<KeyedCoder<?>> asZeroOneBoolean() {
		return equalTo(new FieldCoder<>(key, new ZeroOneBooleanCoder()));
	}

	public Matcher<KeyedCoder<?>> asListOfString() {
		return equalTo(new FieldCoder<>(key, new ListCoder<>(new StringCoder())));
	}

	public <T extends CodeableObjectFactory<?>>  Matcher<KeyedCoder<?>> asObject(Class<T> factoryType) {
		return equalTo(new FieldCoder<>(key, new ObjectCoder<>((CodeableObjectFactory<?>) ReflectionUtils.newInstance(factoryType))));
	}
}
