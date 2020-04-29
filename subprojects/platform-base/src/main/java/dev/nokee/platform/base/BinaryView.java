package dev.nokee.platform.base;

import org.gradle.api.provider.Provider;

import java.util.Set;

/**
 * A view of the binaries that are created and configured as they are required.
 *
 * @param <T> type of the elements in this view
 * @since 0.3
 */
public interface BinaryView<T extends Binary> {

	<S extends T> BinaryView<S> withType(Class<S> type);

	Provider<Set<? extends T>> getElements();
}
