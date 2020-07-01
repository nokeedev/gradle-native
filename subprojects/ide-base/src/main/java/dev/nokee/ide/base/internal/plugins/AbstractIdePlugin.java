package dev.nokee.ide.base.internal.plugins;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.stream.Collectors;

public abstract class AbstractIdePlugin implements Plugin<Project> {
	public static final String IDE_GROUP_NAME = "IDE";
	@Getter  private TaskProvider<Delete> cleanTask;

	@Inject
	protected abstract TaskContainer getTasks();

	@Override
	public final void apply(Project project) {
		cleanTask = getTasks().register(getTaskName("clean"), Delete.class, task -> {
			task.setGroup(IDE_GROUP_NAME);
			task.setDescription("Cleans " + getIdeDisplayName() + " IDE configuration");
		});

		doApply(project);
	}

	private String getTaskName(String verb) {
		return verb + StringUtils.capitalize(getLifecycleTaskName());
	}

	protected abstract void doApply(Project project);

	protected abstract String getLifecycleTaskName();

	protected abstract String getIdeDisplayName();
}
