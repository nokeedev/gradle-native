package dev.nokee.platform.base.internal.tasks;

import dev.nokee.platform.base.ComponentTasks;
import dev.nokee.platform.base.DomainObjectProvider;
import org.gradle.api.Action;
import org.gradle.api.Task;

public interface ComponentTasksInternal extends ComponentTasks {
	<T extends Task> DomainObjectProvider<T> register(TaskName taskName, Class<T> type);
	<T extends Task> DomainObjectProvider<T> register(TaskName taskName, Class<T> type, Action<? super T> action);
	void configureEach(Action<? super Task> action);
}
