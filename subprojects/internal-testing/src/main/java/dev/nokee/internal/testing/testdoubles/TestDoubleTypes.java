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

import java.util.concurrent.Callable;

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

	public static <R extends RawType, RawType, P0> Class<R> ofType(Class<RawType> type, Class<P0> p0Type) {
		return (Class<R>) new TypeToken<R>(type) {} //
			.where(new TypeParameter<P0>() {}, p0Type) //
			.getRawType();
	}
}
