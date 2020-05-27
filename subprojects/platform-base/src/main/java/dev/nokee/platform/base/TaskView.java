package dev.nokee.platform.base;

import org.gradle.api.Task;

/**
 * A view of the tasks that are created and configured as they are required.
 *
 * @param <T> type of the tasks in this view
 * @since 0.3
 */
public interface TaskView<T extends Task> extends View<T> {
}
