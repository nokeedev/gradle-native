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

import java.util.function.Function;

/**
 * A builder to zip multiple provider together.
 *
 * <p>The builder uses {@link ListProperty} to group provider together without loosing any implicit information.
 * For this reason, the builder needs an instance to {@link ObjectFactory}, see {@link #newBuilder(ObjectFactory)}.
 */
public interface ZipProviderBuilder {
	/**
	 * Adds a value to zip.
	 *
	 * @param provider  the value provider
	 * @return a builder for zipping values
	 * @param <T> the value type
	 */
	<T> ZipProviderBuilder value(Provider<? extends T> provider);

	/**
	 * Zip all values using the specified combiner function.
	 *
	 * @param combiner a function used to combine the values
	 * @return a provider containing the result of the combination of all the values
	 * @param <R> the return type
	 */
	<R> Provider<R> zip(Combiner<R> combiner);

	interface ValuesToZip {
		<V> V get(int index);
		Object[] values();
	}

	@FunctionalInterface
	interface Combiner<R> {
		R combine(ValuesToZip values);

		default <V> Combiner<V> andThen(Function<R, V> after) {
			return it -> after.apply(combine(it));
		}
	}

	/**
	 * Creates a new builder with no values to zip.
	 *
	 * @param objects  the object factory to use
	 * @return a no value zip builder
	 */
	static ZipProviderBuilder0 newBuilder(ObjectFactory objects) {
		return new ZipProviderBuilder0(objects);
	}
}
