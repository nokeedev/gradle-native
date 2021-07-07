package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.*;

import static java.util.Objects.requireNonNull;
import static org.gradle.util.GUtil.uncheckedCall;

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

	public static <T> List<T> flatUnpackUntil(@Nullable Object deferred, Class<T> type) {
		return flatUnpackUntil(deferred, DeferredUtils::flatten, DeferredUtils::unpack, type);
	}

	public static <T> List<T> flatUnpackUntil(@Nullable Object deferred, UnaryOperator<Object> unpacker, Class<T> type) {
		return flatUnpackUntil(deferred, DeferredUtils::flatten, unpacker, type);
	}

	/**
	 * Unpack the specified deferred while flattening until all unpacked values are of the specified type.
	 *
	 * @param deferred the deferred object to unpack
	 * @param flatter an flatter consumer for the deferred object
	 * @param unpacker an unpacker operation for the deferred object
	 * @param type the type to unpack
	 * @param <T> the type to unpack
	 * @return a list of {@code T} representing the unpackting of the specified object.
	 * The method returns an empty list if the specified object is null.
	 * @throws IllegalArgumentException if the unpacker operator returns the same object
	 * @throws IllegalStateException if the flatter consumer removes elements from the queue
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> flatUnpackUntil(@Nullable Object deferred, BiFunction<Object, Deque<Object>, Boolean> flatter, UnaryOperator<Object> unpacker, Class<T> type) {
		return (List<T>) flatUnpackWhile(deferred, flatter, it -> unpacker.apply(unpack(it)), it -> {
			if (isFlattenableType(it)) {
				return false;
			}
			if (isNestableDeferred(it)) {
				return true;
			}
			return !type.isInstance(it);
		});
	}

	public static List<Object> flatUnpack(@Nullable Object deferred) {
		return flatUnpackWhile(deferred, DeferredUtils::flatten, DeferredUtils::unpack, DeferredUtils::isNestableDeferred);
	}

	public static List<Object> flatUnpackWhile(@Nullable Object deferred, Predicate<Object> predicate) {
		return flatUnpackWhile(deferred, DeferredUtils::flatten, DeferredUtils::unpack, requireNonNull(predicate));
	}

	public static List<Object> flatUnpackWhile(@Nullable Object deferred, UnaryOperator<Object> unpacker, Predicate<Object> predicate) {
		return flatUnpackWhile(deferred, DeferredUtils::flatten, requireNonNull(unpacker), requireNonNull(predicate));
	}

	// TODO: Add tests
	public static List<Object> flatUnpackWhile(@Nullable Object deferred, BiFunction<Object, Deque<Object>, Boolean> flatter, UnaryOperator<Object> unpacker, Predicate<Object> predicate) {
		if (deferred == null) {
			return ImmutableList.of();
		}

		final ImmutableList.Builder<Object> result = ImmutableList.builder();
		final Deque<Object> queue = new ArrayDeque<>();
		queue.addFirst(deferred);
		while (!queue.isEmpty()) {
			Object value = queue.removeFirst();
			if (predicate.test(value)) {
				queue.addFirst(unpacker.apply(value));
			} else {
				val sizeBefore = queue.size();
				val didFlat = flatter.apply(value, queue);
				if (sizeBefore > queue.size()) {
					throw new IllegalStateException("Flatter consumer cannot remove items from the queue");
				} else if (!didFlat) {
					result.add(value);
				}
			}
		}
		return result.build();
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
		}
		return false;
	}

	static boolean isFlattenableType(Object value) {
		return value instanceof List || value instanceof Set || value instanceof Object[];
	}

	private static void addAllFirst(Deque<Object> queue, Object[] items) {
		for (int i = items.length - 1; i >= 0; i--) {
			queue.addFirst(items[i]);
		}
	}

	static boolean isDeferred(Object value) {
		return value instanceof Provider
			|| isNestableDeferred(value);
	}

	static boolean isNestableDeferred(@Nullable Object value) {
		return value instanceof Callable
			|| value instanceof Supplier
			|| isKotlinFunction0Deferrable(value);
	}

	@Nullable
	static Object unpackNestableDeferred(Object deferred) {
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
		return KOTLIN_FUNCTION0_CLASS.map(it -> it.isAssignableFrom(value.getClass())).orElse(false);
	}

	@Nullable
	private static Object unpackKotlinFunction0(Object value) {
		try {
			return KOTLIN_FUNCTION0_CLASS.orElseThrow(() -> new RuntimeException("kotlin.jvm.functions.Function0 not found in classpath.")).getMethod("invoke").invoke(value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(String.format("Could not access kotlin.jvm.functions.Function0#invoke() method on object of type '%s'.", value.getClass().getCanonicalName()), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(String.format("Could not invoke kotlin.jvm.functions.Function0#invoke() method on object of type '%s'.", value.getClass().getCanonicalName()), e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(String.format("Could not find kotlin.jvm.functions.Function0#invoke() method on object of type '%s'.", value.getClass().getCanonicalName()), e);
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
