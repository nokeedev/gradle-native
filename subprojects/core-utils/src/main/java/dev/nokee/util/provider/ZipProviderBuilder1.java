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

import com.google.common.collect.ImmutableList;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

/**
 * Convenient builder when a single value was specified.
 *
 * @param <T>  the first value type
 */
public final class ZipProviderBuilder1<T> implements ZipProviderBuilder {
	private final ObjectFactory objects;
	private final Provider<? extends T> firstValue;

	// Use ZipProviderBuilder#newBuilder(objects)
	ZipProviderBuilder1(ObjectFactory objects, Provider<? extends T> firstValue) {
		this.objects = objects;
		this.firstValue = firstValue;
	}

	@Override
	public <S> ZipProviderBuilder2<T, S> value(Provider<? extends S> provider) {
		return new ZipProviderBuilder2<>(objects, firstValue, provider);
	}

	public Provider<? extends T> zip() {
		return firstValue; // Only one value, return that provider
	}

	@Override
	public <R> Provider<R> zip(Combiner<R> combiner) {
		// This is basically Provider#map
		return firstValue.map(it -> new DefaultValuesToZip(ImmutableList.of(it))).map(combiner::combine);
	}
}
