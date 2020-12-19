package dev.nokee.model.internal.type;

import com.google.common.reflect.TypeToken;

/**
 * Extract a type token of a complex type.
 *
 * @param <T> the type
 * @see TypeToken
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class TypeOf<T> {
	final TypeToken<T> type = new TypeToken<T>(getClass()) {};
}
