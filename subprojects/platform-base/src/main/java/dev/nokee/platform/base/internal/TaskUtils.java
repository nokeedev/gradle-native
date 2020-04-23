package dev.nokee.platform.base.internal;

import org.gradle.api.Action;
import org.gradle.api.Task;

public class TaskUtils {
	public static <T extends Task> Action<T> dependsOn(Object... tasks) {
		return task -> task.dependsOn(tasks);
	}
}
