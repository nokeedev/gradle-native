package dev.nokee.platform.base;

import org.gradle.api.Action;
import org.gradle.api.provider.Provider;

import java.util.Set;

/**
 * A view of the binaries that are created and configured as they are required.
 *
 * @param <T> type of the elements in this view
 * @since 0.3
 */
public interface BinaryView<T extends Binary> {
	/**
	 * Registers an action to execute to configure each binary in the view.
	 * The action is only executed for those binaries that are required.
	 * Fails if any binary has already been finalized.
	 *
	 * @param action The action to execute on each binary for configuration.
	 */
	void configureEach(Action<? super T> action);

	/**
	 * Registers an action to execute to configure each binary in the view.
	 * The action is only executed for those elements that are required.
	 * Fails if any matching element has already been finalized.
	 *
	 * This method is equivalent to <code>binaries.withType(Foo).configureEach { ... }</code>.
	 *
	 * @param type The type of binary to select.
	 * @param <S> The base type of the binary to configure.
	 * @param action The action to execute on each element for configuration.
	 */
	<S extends T> void configureEach(Class<S> type, Action<? super S> action);

	/**
	 * Returns a binary view containing the objects in this collection of the given type.
	 * The returned collection is live, so that when matching objects are later added to this collection, they are also visible in the filtered binary view.
	 *
	 * @param type The type of binary to find.
	 * @param <S> The base type of the new binary view.
	 * @return The matching binaries. Returns an empty collection if there are no such objects in this collection.
	 */
	<S extends T> BinaryView<S> withType(Class<S> type);

	/**
	 * Returns the contents of this view as a {@link Provider} of {@code <T>} instances.
	 *
	 * <p>The returned {@link Provider} is live, and tracks changes of the view.</p>
	 *
	 * @return a provider containing all the elements included in this view.
	 */
	Provider<Set<? extends T>> getElements();
}
