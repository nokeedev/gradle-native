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

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;

import java.util.function.BiFunction;

/**
 * Convenient builder when zipping two values.
 * It stands as a backward compatible {@code Provider#zip}.
 *
 * @param <T>  the first value type
 * @param <S>  the second value type
 */
public final class ZipProviderBuilder2<T, S> implements ZipProviderBuilder {
	private final ObjectFactory objects;
	private final Provider<? extends T> firstValue;
	private final Provider<? extends S> secondValue;

	// Use ZipProviderBuilder#newBuilder(objects)
	ZipProviderBuilder2(ObjectFactory objects, Provider<? extends T> firstValue, Provider<? extends S> secondValue) {
		this.objects = objects;
		this.firstValue = firstValue;
		this.secondValue = secondValue;
	}

	@Override
	public <U> ZipProviderBuilder value(Provider<? extends U> provider) {
		return new ZipProviderBuilderX(objects).value(firstValue).value(secondValue).value(provider);
	}

	public <R> Provider<R> zip(BiFunction<? super T, ? super S, ? extends R> combiner) {
		return zip(values -> combiner.apply(values.get(0), values.get(1)));
	}

	@Override
	public <R> Provider<R> zip(Combiner<R> combiner) {
		final ListProperty<Object> accumulator = objects.listProperty(Object.class);
		accumulator.add(firstValue);
		accumulator.add(secondValue);
		return accumulator.map(DefaultValuesToZip::new).map(combiner::combine);
	}
}
