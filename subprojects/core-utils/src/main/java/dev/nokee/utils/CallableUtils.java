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
package dev.nokee.utils;

import com.google.common.base.Suppliers;
import dev.nokee.util.lambdas.SerializableCallable;
import dev.nokee.util.lambdas.internal.SerializableCallableAdapter;
import lombok.EqualsAndHashCode;
import org.gradle.internal.UncheckedException;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.concurrent.Callable;

public final class CallableUtils {
	private CallableUtils() {}

	static final ExceptionHandler DEFAULT_EXCEPTION_HANDLER = new ExceptionHandler() {
		@Override
		public RuntimeException rethrowAsUncheckedException(Throwable t) {
			throw UncheckedException.throwAsUncheckedException(t);
		}
	};
	private static final UncheckedCaller UNCHECKED_CALLER = new DefaultUncheckedCaller(DEFAULT_EXCEPTION_HANDLER);

	/**
	 * Same as {@code unchecked().call(callable)}.
	 */
	@Nullable
	public static <ReturnType> ReturnType uncheckedCall(Callable<ReturnType> self) {
		return unchecked().call(self);
	}

	/**
	 * Returns an unchecked caller service for {@link Callable}.
	 *
	 * @return unchecked caller service, never null
	 */
	public static UncheckedCaller unchecked() {
		return UNCHECKED_CALLER;
	}

	public interface UncheckedCaller {
		@Nullable
		<ReturnType> ReturnType call(Callable<ReturnType> callable);
	}

	public interface ExceptionHandler {
		RuntimeException rethrowAsUncheckedException(Throwable t);
	}

	@EqualsAndHashCode
	public static final class DefaultUncheckedCaller implements UncheckedCaller {
		private final ExceptionHandler handler;

		public DefaultUncheckedCaller(ExceptionHandler handler) {
			this.handler = handler;
		}

		@Override
		public <ReturnType> ReturnType call(Callable<ReturnType> self) {
			try {
				return self.call();
			} catch (Exception e) {
				throw handler.rethrowAsUncheckedException(e);
			}
		}
	}

	public static <V> Callable<V> memoizeOnCall(Callable<V> delegate) {
		return Suppliers.memoize(() -> uncheckedCall(delegate))::get;
	}

	/**
	 * Mechanism to create {@link Callable} from Java lambdas that are {@link Serializable}.
	 *
	 * <p><b>Note:</b> The returned {@code Callable} will provide an {@link Object#equals(Object)}/{@link Object#hashCode()} implementation based on the serialized bytes.
	 * The goal is to ensure the Java lambdas can be compared between each other after deserialization.
	 */
	public static <ReturnType> Callable<ReturnType> ofSerializableCallable(SerializableCallable<ReturnType> callable) {
		return new SerializableCallableAdapter<>(callable);
	}
}
