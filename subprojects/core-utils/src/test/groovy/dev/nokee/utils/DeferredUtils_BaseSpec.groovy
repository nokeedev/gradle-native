/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.utils


import kotlin.jvm.functions.Function0
import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.function.Supplier

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory

class DeferredUtils_BaseSpec extends Specification {
	protected Property<String> propertyOf(String value) {
		return propertyOf(String, value)
	}

	protected <T> Property<T> propertyOf(Class<T> type, T value) {
		return objectFactory().property(type).value(providerOf(value))
	}

	protected <T> Provider<T> providerOf(T value) {
		return providerFactory().provider { ThrowIfResolvedGuard.resolve(value) }
	}

	protected <T> Supplier<T> supplierOf(T value) {
		return new Supplier<T>() {
			@Override
			T get() {
				return ThrowIfResolvedGuard.resolve(value)
			}
		}
	}

	protected <T> Closure<T> closureOf(T value) {
		return { ThrowIfResolvedGuard.resolve(value) }
	}

	protected <T> Function0<T> kotlinFunctionOf(T value) {
		return new Function0<T>() {
			@Override
			T invoke() {
				return ThrowIfResolvedGuard.resolve(value)
			}
		}
	}

	protected <T> Callable<T> callableOf(T value) {
		return new Callable<T>() {
			@Override
			T call() throws Exception {
				return ThrowIfResolvedGuard.resolve(value)
			}
		}
	}

	protected static Named namedElement(String name) {
		return new Named() {
			@Override
			String getName() {
				return name
			}
		}
	}

	protected static ThrowIfResolvedGuard throwIfResolved() {
		return new ThrowIfResolvedGuard()
	}

	private static class ThrowIfResolvedGuard {
		static <T> T resolve(T obj) {
			if (obj instanceof ThrowIfResolvedGuard) {
				throw new AssertionError((Object)"Should not resolve")
			}
			return obj;
		}
	}
}
