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

import com.google.common.collect.Streams;
import org.hamcrest.Matcher;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public final class PBXObjectMatchers {
	public static <T> Matcher<T> matchesObject(@Nullable T expectedValue) {
		return expectedValue == null ? allOf(nullValue()) : equalTo(expectedValue);
	}

	public static Matcher<Iterable<?>> matchesIterable(@Nullable Iterable<?> expectedValue) {
		return expectedValue == null ? emptyIterable() : contains(Streams.stream((Iterable<?>) expectedValue).toArray());
	}

	@SuppressWarnings("unchecked")
	public static Matcher<Map<?, ?>> matchesMap(@Nullable Map<?, ?> expectedValue) {
		return expectedValue == null ? anEmptyMap() :
			allOf(((Map<Object, Object>) expectedValue).entrySet().stream().map(it -> hasEntry(it.getKey(), it.getValue())).collect(Collectors.toList()));
	}

	public static <T> Matcher<Optional<T>> matchesOptional(@Nullable T expectedValue) {
		return expectedValue == null ? emptyOptional() : allOf(optionalWithValue(equalTo(expectedValue)));
	}

	public static Matcher<Boolean> matchesBoolean(@Nullable Boolean expectedValue, boolean defaultValue) {
		return expectedValue == null ? is(defaultValue) : is(expectedValue);
	}
}
