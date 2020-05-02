package dev.nokee.platform.base;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;

import java.util.Set;

/**
 * A view of the tasks that are created and configured as they are required.
 *
 * @param <T> type of the tasks in this view
 * @since 0.3
 */
public interface TaskView<T extends Task> {
	/**
	 * Registers an action to execute to configure each task in the view.
	 * The action is only executed for those tasks that are required.
	 * Fails if any task has already been finalized.
	 *
	 * @param action The action to execute on each task for configuration.
	 */
	void configureEach(Action<? super T> action);

	/**
	 * Returns the contents of this view as a {@link Provider} of {@code <T>} instances.
	 *
	 * <p>The returned {@link Provider} is live, and tracks changes of the view.</p>
	 *
	 * @return a provider containing all the elements included in this view.
	 */
	Provider<Set<? extends T>> getElements();
}
