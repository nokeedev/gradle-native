package dev.nokee.platform.base;

import org.gradle.api.Task;

/**
 * A view of the tasks that are created and configured as they are required.
 *
 * @param <T> type of the tasks in this view
 * @since 0.3
 */
public interface TaskView<T extends Task> extends View<T> {
	/**
	 * Returns a task view containing the objects in this view of the given type.
	 * The returned view is live, so that when matching objects are later added to this view, they are also visible in the filtered task view.
	 *
	 * @param type The type of task to find.
	 * @param <S> The base type of the new task view.
	 * @return the matching element as a {@link TaskView}, never null.
	 */
	<S extends T> TaskView<S> withType(Class<S> type);
}
