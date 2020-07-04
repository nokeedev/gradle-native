package dev.nokee.ide.base.internal;

import org.gradle.api.Describable;
import org.gradle.api.Rule;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import javax.inject.Inject;

public abstract class IdeBridgeRule<T extends IdeRequest> implements Rule {
	private final Describable ide;

	public IdeBridgeRule(Describable ide) {
		this.ide = ide;
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Override
	public String getDescription() {
		return String.format("%s IDE bridge tasks begin with %s. Do not call these directly.", ide.getDisplayName(), getLifecycleTaskNamePrefix());
	}

	protected abstract String getLifecycleTaskNamePrefix();

	@Override
	public void apply(String taskName) {
		if (taskName.startsWith(getLifecycleTaskNamePrefix())) {
			T request = newRequest(taskName);
			if (request.getAction().equals(IdeRequestAction.CLEAN)) {
				Task bridgeTask = getTasks().create(request.getTaskName());
				bridgeTask.dependsOn(LifecycleBasePlugin.CLEAN_TASK_NAME);
			} else {
				doHandle(request);
			}
		}
	}

	public abstract T newRequest(String taskName);

	protected abstract void doHandle(T request);
}
