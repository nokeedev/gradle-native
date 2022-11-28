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

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.SerializationUtils;
import org.gradle.internal.UncheckedException;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
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

	/**
	 * Mechanism to create {@link Callable} from Java lambdas that are {@link Serializable}.
	 *
	 * <p><b>Note:</b> The returned {@code Callable} will provide an {@link Object#equals(Object)}/{@link Object#hashCode()} implementation based on the serialized bytes.
	 * The goal is to ensure the Java lambdas can be compared between each other after deserialization.
	 */
	public static <ReturnType> Callable<ReturnType> ofSerializableCallable(SerializableCallable<ReturnType> callable) {
		return new SerializableCallableAdapter<>(callable);
	}

	/**
	 * A {@link Serializable} version of {@link Callable}.
	 */
	public interface SerializableCallable<V> extends Callable<V>, Serializable {}

	private static final class SerializableCallableAdapter<V> implements Callable<V>, Serializable {
		private final SerializableCallable<V> delegate;

		public SerializableCallableAdapter(SerializableCallable<V> delegate) {
			this.delegate = delegate;
		}

		@Override
		public V call() throws Exception {
			return delegate.call();
		}

		// The following equals and hashCode is not the most efficient implementation
		//   but is a good enough implementation... for now.
		// When-if these become a measurable bottleneck, we can revisit those implementation.
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof SerializableCallableAdapter)) {
				return false;
			}
			SerializableCallableAdapter<?> that = (SerializableCallableAdapter<?>) o;
			return Arrays.equals(SerializationUtils.serialize(delegate), SerializationUtils.serialize(that.delegate));
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(SerializationUtils.serialize(delegate));
		}
	}
}
