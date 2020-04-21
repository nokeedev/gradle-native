package dev.nokee.platform.base;

import org.gradle.api.Action;

/**
 * A view of the variants that are created and configured as they are required.
 *
 * @param <T> type of the elements in this container
 */
public interface VariantView<T extends Variant> {
	/**
	 * Registers an action to execute to configure each element in the view. The action is only executed for those elements that are required. Fails if any element has already been finalized.
	 *
	 * @param action The action to execute on each element for configuration.
	 */
	void configureEach(Action<? super T> action);
}
