package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.Variant;

/**
 * A factory for variant objects of type {@code T}.
 *
 * @param <T> The type of objects which this factory creates.
 */
public interface VariantFactory<T extends Variant> {
	T create(String name, BuildVariant buildVariant);
}
