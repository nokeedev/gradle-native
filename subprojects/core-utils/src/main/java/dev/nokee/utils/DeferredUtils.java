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
package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import dev.nokee.util.internal.AlwaysFalsePredicate;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.not;
import static dev.nokee.utils.CallableUtils.uncheckedCall;

public final class DeferredUtils {
	private static final Optional<Class<?>> KOTLIN_FUNCTION0_CLASS = loadKotlinFunction0Class();

	/**
	 * Successively unpacks a deferred value until it is resolved to null or something other than Callable (including Groovy Closure) or Kotlin lambda or Supplier
	 * then unpacks the remaining Provider or Factory.
	 *
	 * @param deferred  the deferred object to unpack, may be null
	 * @return the unpacked object
	 */
	@Nullable
	public static Object unpack(@Nullable Object deferred) {
		if (deferred == null) {
			return null;
		}
		Object value = unpackNestableDeferred(deferred);
		if (value instanceof Provider) {
			return ((Provider<?>) value).get();
		}
		return value;
	}

	public static <T> UnpackingBuilder<T> unpack(Unpacker unpacker) {
		return new UnpackingBuilder<T>() {
			@Override
			public Executable<T> until(Class<?> type) {
				return new UnpackWhileExecutable<>(unpacker, DeferredUtils.until(type));
			}

			@Override
			public Executable<T> whileTrue(Predicate<Object> predicate) {
				return new UnpackWhileExecutable<>(unpacker, predicate);
			}

			@Override
			public T execute(@Nullable Object obj) {
				return new UnpackWhileExecutable<T>(unpacker, AlwaysFalsePredicate.alwaysFalse()).execute(obj);
			}
		};
	}

	@EqualsAndHashCode
	public static final class UnpackWhileExecutable<T> implements Executable<T> {
		private final Unpacker unpacker;
		private final Predicate<Object> predicate;

		public UnpackWhileExecutable(Unpacker unpacker, Predicate<Object> predicate) {
			this.unpacker = unpacker;
			this.predicate = predicate;
		}

		@Override
		public T execute(@Nullable Object obj) {
			while (predicate.test(obj)) {
				obj = unpacker.unpack(obj);
			}
			@SuppressWarnings("unchecked")
			final T result = (T) obj;
			return result;
		}
	}

	public interface Flattener {
		List<Object> flatten(@Nullable Object target);
	}

	public interface Unpacker {
		@Nullable
		Object unpack(@Nullable Object target);
	}

	public static final Flattener DEFAULT_FLATTENER = new Flattener() {
		@Override
		public List<Object> flatten(Object value) {
			final ImmutableList.Builder<Object> result = ImmutableList.builder();
			if (value instanceof Iterable) {
				result.addAll((Iterable<?>) value);
			} else if (value instanceof Object[]) {
				result.add((Object[]) value);
			} else {
				result.add(value);
			}
			return result.build();
		}
	};

	public static FlatteningBuilder flat() {
		return new FlatBuilder(DEFAULT_FLATTENER);
	}

	public static FlatteningBuilder flat(Flattener flattener) {
		return new FlatBuilder(flattener);
	}

	public static List<Object> flatten(@Nullable Object value) {
		return flat().execute(value);
	}

	public static <T> UnpackingBuilder<List<T>> flatUnpack() {
		return new FlatUnpackBuilder<>(DEFAULT_FLATTENER, DEFAULT_UNPACKER);
	}

	public static <T> UnpackingBuilder<List<T>> flatUnpack(Unpacker unpacker) {
		return new FlatUnpackBuilder<>(DEFAULT_FLATTENER, unpacker);
	}

	public static List<Object> flatUnpack(@Nullable Object deferred) {
		return flatUnpack().execute(deferred);
	}

	public static <T> Executable<List<T>> flatUnpackUntil(Class<T> type) {
		return new FlatUnpackWhileExecutable<>(DEFAULT_FLATTENER, DEFAULT_UNPACKER, until(type));
	}

	public static <T> Executable<List<T>> flatUnpackWhile(Predicate<Object> predicate) {
		return new FlatUnpackWhileExecutable<>(DEFAULT_FLATTENER, DEFAULT_UNPACKER, predicate);
	}

	interface ConditionalBuilder<R> {
		Executable<R> until(Class<?> type);
		Executable<R> whileTrue(Predicate<Object> predicate);
	}

	public interface FlatteningBuilder extends ConditionalBuilder<List<Object>>, Executable<List<Object>> {
		UnpackingBuilder<List<Object>> unpack();
		UnpackingBuilder<List<Object>> unpack(Unpacker f);
	}

	public static final Unpacker DEFAULT_UNPACKER = new Unpacker() {
		@Override
		public @Nullable Object unpack(Object target) {
			return DeferredUtils.unpack(target);
		}
	};

	public static final Unpacker IDENTITY_UNPACKER = new Unpacker() {
		@Override
		public @Nullable Object unpack(@Nullable Object target) {
			return target;
		}
	};

	@EqualsAndHashCode
	public static final class FlatBuilder implements FlatteningBuilder {
		private final Flattener flattener;

		public FlatBuilder(Flattener flattener) {
			this.flattener = flattener;
		}

		@Override
		public Executable<List<Object>> until(Class<?> type) {
			return new FlatUnpackWhileExecutable<>(flattener, IDENTITY_UNPACKER, DeferredUtils.until(type));
		}

		@Override
		public Executable<List<Object>> whileTrue(Predicate<Object> predicate) {
			return new FlatUnpackWhileExecutable<>(flattener, IDENTITY_UNPACKER, predicate);
		}

		@Override
		public UnpackingBuilder<List<Object>> unpack() {
			return new FlatUnpackBuilder<>(flattener, DEFAULT_UNPACKER);
		}

		@Override
		public UnpackingBuilder<List<Object>> unpack(Unpacker unpacker) {
			return new FlatUnpackBuilder<>(flattener, unpacker);
		}

		@Override
		public List<Object> execute(@Nullable Object obj) {
			return flatUnpackWhile(obj, flattener, IDENTITY_UNPACKER, DeferredUtils::isFlattenableType);
		}
	}

	public interface UnpackingBuilder<R> extends ConditionalBuilder<R>, Executable<R> {}

	@EqualsAndHashCode
	public static final class FlatUnpackBuilder<T> implements UnpackingBuilder<List<T>> {
		private final Flattener flattener;
		private final Unpacker unpacker;

		public FlatUnpackBuilder(Flattener flattener, Unpacker unpacker) {
			this.flattener = flattener;
			this.unpacker = unpacker;
		}

		@Override
		public Executable<List<T>> until(Class<?> type) {
			return new FlatUnpackWhileExecutable<>(flattener, unpacker, DeferredUtils.until(type));
		}

		@Override
		public Executable<List<T>> whileTrue(Predicate<Object> predicate) {
			return new FlatUnpackWhileExecutable<>(flattener, unpacker, predicate);
		}

		@Override
		public List<T> execute(@Nullable Object obj) {
			@SuppressWarnings("unchecked")
			List<T> result = (List<T>) flatUnpackWhile(obj, flattener, unpacker, it -> DeferredUtils.isNestableDeferred(it) || DeferredUtils.isFlattenableType(it));
			return result;
		}
	}

	public interface Executable<R> {
		R execute(@Nullable Object obj);
	}

	@EqualsAndHashCode
	public static final class FlatUnpackWhileExecutable<T> implements Executable<List<T>> {
		private final Flattener flattener;
		private final Unpacker unpacker;
		private final Predicate<Object> predicate;

		public FlatUnpackWhileExecutable(Flattener flattener, Unpacker unpacker, Predicate<Object> predicate) {
			this.flattener = flattener;
			this.unpacker = unpacker;
			this.predicate = predicate;
		}

		@Override
		public List<T> execute(@Nullable Object obj) {
			@SuppressWarnings("unchecked")
			final List<T> result = (List<T>) flatUnpackWhile(obj, flattener, unpacker, predicate);
			return result;
		}
	}

	private static Predicate<Object> until(Class<?> type) {
		return not(instanceOf(type));
	}

	private static List<Object> flatUnpackWhile(@Nullable Object deferred, Flattener flattener, Unpacker unpacker, Predicate<Object> predicate) {
		if (deferred == null) {
			return ImmutableList.of();
		}

		final ImmutableList.Builder<Object> builder = ImmutableList.builder();
		final Deque<Object> queue = new ArrayDeque<>();
		queue.addFirst(deferred);
		while (!queue.isEmpty()) {
			Object value = queue.removeFirst();
			// Check condition first in case the pre-unpack type is the one we are looking for (after flattening for example)
			if (predicate.test(value)) {
				value = unpacker.unpack(value);

				// Check condition again in case the unpacked type is the one we are looking for BUT is also a flattening type
				if (predicate.test(value)) {
					final List<?> list = flattener.flatten(value);
					final ListIterator<?> iterator = list.listIterator(list.size());
					while (iterator.hasPrevious()) {
						final Object item = iterator.previous();
						queue.addFirst(item);
					}
				} else {
					builder.add(value);
				}
			} else {
				builder.add(value);
			}
		}

		return builder.build();
	}

	static boolean flatten(Object value, Deque<Object> queue) {
		if (value instanceof List) {
			List<?> list = (List<?>) value;
			if (list instanceof RandomAccess) {
				for (int i = list.size() - 1; i >= 0; i--) {
					queue.addFirst(list.get(i));
				}
			} else {
				ListIterator<?> iterator = list.listIterator(list.size());
				while (iterator.hasPrevious()) {
					Object item = iterator.previous();
					queue.addFirst(item);
				}
			}
			return true;
		} else if (value instanceof Set) {
			((Set<?>) value).forEach(queue::addFirst);
			return true;
		} else if (value instanceof Object[]) {
			Object[] array = (Object[]) value;
			addAllFirst(queue, array);
			return true;
		} else if (value instanceof Iterable) {
			return flatten(ImmutableList.copyOf((Iterable<?>) value), queue); // Preserve ordering
		}
		return false;
	}

	public static boolean isFlattenableType(Object value) {
		return value instanceof Object[] || value instanceof Iterable;
	}

	private static void addAllFirst(Deque<Object> queue, Object[] items) {
		for (int i = items.length - 1; i >= 0; i--) {
			queue.addFirst(items[i]);
		}
	}

	public static boolean isDeferred(Object value) {
		return value instanceof Provider
			|| isNestableDeferred(value);
	}

	static boolean isNestableDeferred(@Nullable Object value) {
		return value instanceof Callable
			|| value instanceof Supplier
			|| isKotlinFunction0Deferrable(value);
	}

	@Nullable
	static Object unpackNestableDeferred(@Nullable Object deferred) {
		Object current = deferred;
		while (isNestableDeferred(current)) {
			if (current instanceof Callable) {
				current = uncheckedCall((Callable<?>) current);
			} else if (current instanceof Supplier) {
				current = ((Supplier<?>) current).get();
			} else {
				current = unpackKotlinFunction0(current);
			}
		}
		return current;
	}

	private static boolean isKotlinFunction0Deferrable(@Nullable Object value) {
		return KOTLIN_FUNCTION0_CLASS.map(it -> value != null && it.isAssignableFrom(value.getClass())).orElse(false);
	}

	@Nullable
	private static Object unpackKotlinFunction0(@Nullable Object value) {
		try {
			return KOTLIN_FUNCTION0_CLASS.orElseThrow(() -> new RuntimeException("kotlin.jvm.functions.Function0 not found in classpath.")).getMethod("invoke").invoke(value);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(String.format("Could not access kotlin.jvm.functions.Function0#invoke() method on object of type '%s'.", Optional.ofNullable(value).map(it -> it.getClass().getCanonicalName()).orElse("<null>")), e);
		}
	}

	private static Optional<Class<?>> loadKotlinFunction0Class() {
		try {
			return Optional.of(Class.forName("kotlin.jvm.functions.Function0"));
		} catch (ClassNotFoundException e) {
			return Optional.empty();
		}
	}

	/**
	 * Realize the specified domain object collection.
	 * The implementation of this method is dependent on the implementation of the domain object collections.
	 * This method will always force realize all elements of the collection.
	 * @param collection the collection to realize
	 * @param <T> the type of the collection to realize
	 */
	public static <T> void realize(DomainObjectCollection<T> collection) {
		val iter = collection.iterator();
		if (iter.hasNext()) {
			iter.next();
		}
	}
}
