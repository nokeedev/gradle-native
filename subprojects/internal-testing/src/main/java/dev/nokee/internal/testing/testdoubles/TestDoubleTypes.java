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
package dev.nokee.internal.testing.testdoubles;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public final class TestDoubleTypes {
	public static <V> Class<Callable<V>> ofCallable(Class<V> type) {
		return (Class<Callable<V>>) new TypeToken<Callable<V>>() {}.where(new TypeParameter<V>() {}, type).getRawType();
	}

	public static <OUT, IN> Class<Transformer<OUT, IN>> ofTransformer(Class<OUT> outType, Class<IN> inType) {
		return (Class<Transformer<OUT, IN>>) new TypeToken<Transformer<OUT, IN>>() {} //
			.where(new TypeParameter<OUT>() {}, outType) //
			.where(new TypeParameter<IN>() {},  inType) //
			.getRawType();
	}

	public static <T> Class<Action<T>> ofAction(Class<T> type) {
		return (Class<Action<T>>) new TypeToken<Action<T>>() {} //
			.where(new TypeParameter<T>() {}, type) //
			.getRawType();
	}

	public static <T, R> Class<Function<T, R>> ofFunction(Class<T> tType, Class<R> rType) {
		return (Class<Function<T, R>>) new TypeToken<Function<T, R>>() {} //
			.where(new TypeParameter<T>() {}, tType) //
			.where(new TypeParameter<R>() {},  rType) //
			.getRawType();
	}

	public static <T> Class<Consumer<T>> ofConsumer(Class<T> tType) {
		return (Class<Consumer<T>>) new TypeToken<Consumer<T>>() {} //
			.where(new TypeParameter<T>() {}, tType) //
			.getRawType();
	}

	public static <T, U> Class<BiConsumer<T, U>> ofBiConsumer(Class<T> tType, Class<U> uType) {
		return (Class<BiConsumer<T, U>>) new TypeToken<BiConsumer<T, U>>() {} //
			.where(new TypeParameter<T>() {}, tType) //
			.where(new TypeParameter<U>() {}, uType) //
			.getRawType();
	}

	public static <R extends RawType, RawType, P0> Class<R> ofType(Class<RawType> type, Class<P0> p0Type) {
		return (Class<R>) new TypeToken<R>(type) {} //
			.where(new TypeParameter<P0>() {}, p0Type) //
			.getRawType();
	}

	public static <T> Class<Spec<T>> ofSpec(Class<T> type) {
		return (Class<Spec<T>>) new TypeToken<Spec<T>>() {} //
			.where(new TypeParameter<T>() {}, type) //
			.getRawType();
	}

	public static <T> Class<Provider<T>> ofProvider(Class<T> type) {
		return (Class<Provider<T>>) new TypeToken<Provider<T>>() {} //
			.where(new TypeParameter<T>() {}, type) //
			.getRawType();
	}

	public static <T> Class<Iterable<T>> ofIterable(Class<T> type) {
		return (Class<Iterable<T>>) new TypeToken<Iterable<T>>() {} //
			.where(new TypeParameter<T>() {}, type) //
			.getRawType();
	}

	public static <T, R> Class<TestClosure<R, T>> ofClosure(Class<T> firstArgument) {
		return (Class<TestClosure<R, T>>) new TypeToken<TestClosure<R, T>>() {}
			.where(new TypeParameter<T>() {}, firstArgument)
			.getRawType();
	}

	public static <T, U, R> Class<TestBiClosure<R, T, U>> ofClosure(Class<T> firstArgument, Class<U> secondArgument) {
		return (Class<TestBiClosure<R, T, U>>) new TypeToken<TestBiClosure<R, T, U>>() {}
			.where(new TypeParameter<T>() {}, firstArgument)
			.where(new TypeParameter<U>() {}, secondArgument)
			.getRawType();
	}
}
