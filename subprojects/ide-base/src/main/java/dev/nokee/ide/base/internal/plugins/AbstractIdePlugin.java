package dev.nokee.ide.base.internal.plugins;

import dev.nokee.ide.base.internal.IdeWorkspace;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.*;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.logging.ConsoleRenderer;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.util.stream.Collectors;

public abstract class AbstractIdePlugin implements Plugin<Project> {
	public static final String IDE_GROUP_NAME = "IDE";
	@Getter private TaskProvider<Delete> cleanTask;
	@Getter private TaskProvider<Task> lifecycleTask;

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ExecOperations getExecOperations();

	@Override
	public final void apply(Project project) {
		cleanTask = getTasks().register(getTaskName("clean"), Delete.class, task -> {
			task.setGroup(IDE_GROUP_NAME);
			task.setDescription("Cleans " + getIdeDisplayName() + " IDE configuration");
		});

		lifecycleTask = getTasks().register(getLifecycleTaskName(), task -> {
			task.setGroup(IDE_GROUP_NAME);
			task.setDescription("Generates " + getIdeDisplayName() + " IDE configuration");
			task.shouldRunAfter(cleanTask);
		});

		doApply(project);
	}

	protected void addWorkspace(IdeWorkspace workspace) {
		// Lifecycle configuration
		getLifecycleTask().configure(task -> {
			task.dependsOn(workspace.getGeneratorTask());
			task.doLast(new Action<Task>() {
				@Override
				public void execute(Task task) {
					task.getLogger().lifecycle(String.format("Generated %s at %s", workspace.getDisplayName(), new ConsoleRenderer().asClickableFileUrl(workspace.getLocation().get().getAsFile())));
				}
			});
		});

		// Clean configuration
		workspace.getGeneratorTask().configure(task -> task.shouldRunAfter(getCleanTask()));

		// Open configuration
		getTasks().register(getTaskName("open"), task -> {
			task.dependsOn(getLifecycleTask());
			task.setGroup(IDE_GROUP_NAME);
			task.setDescription("Opens the " + workspace.getDisplayName());
			task.doLast(new Action<Task>() {
				@Override
				public void execute(Task task) {
					if (SystemUtils.IS_OS_MAC) {
						getExecOperations().exec(spec -> spec.commandLine("open", workspace.getLocation().get()));
					} else {
						try {
							Desktop.getDesktop().open(workspace.getLocation().get().getAsFile());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				}
			});
		});
	}

	private String getTaskName(String verb) {
		return verb + StringUtils.capitalize(getLifecycleTaskName());
	}

	protected abstract void doApply(Project project);

	protected abstract String getLifecycleTaskName();

	protected abstract String getIdeDisplayName();
}
