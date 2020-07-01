package dev.nokee.ide.base.internal.plugins;

import dev.nokee.ide.base.internal.BaseIdeProjectMetadata;
import dev.nokee.ide.base.internal.IdeProjectExtension;
import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.base.internal.IdeWorkspaceInternal;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.*;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.logging.ConsoleRenderer;
import org.gradle.plugins.ide.internal.IdeArtifactRegistry;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public abstract class AbstractIdePlugin implements Plugin<Project> {
	public static final String IDE_GROUP_NAME = "IDE";
	@Getter private TaskProvider<Delete> cleanTask;
	@Getter private TaskProvider<Task> lifecycleTask;

	@Override
	public final void apply(Project project) {
		this.project = project;

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

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ExecOperations getExecOperations();

	@Inject
	protected abstract IdeArtifactRegistry getArtifactRegistry();

	@Getter(AccessLevel.PROTECTED)
	private Project project;

	protected void addProjectExtension(IdeProjectExtension extension) {
		// Since all Xcode components are expected to be registered lazily, we can't register the IDE project inside the configuration action above.
		// Instead, we rely on the schema after the project is evaluated and register metadata using the provider.
		// For better laziness, we should disallow all eager method from the containers (aka using a custom container).
		// We should also disallow any modification after we read the collection schema.
		project.afterEvaluate(proj -> {
			extension.getProjects().getCollectionSchema().getElements().forEach(element -> {
				getArtifactRegistry().registerIdeProject(newIdeProjectMetadata(extension.getProjects().named(element.getName()).map(IdeProjectInternal.class::cast)));
			});
		});
	}

	protected abstract IdeProjectMetadata newIdeProjectMetadata(Provider<IdeProjectInternal> ideProject);

	protected void addWorkspace(IdeWorkspaceInternal workspace) {
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

	/**
	 * Returns the path to the correct Gradle distribution to use.
	 * The wrapper of the generating project will be used only if the execution context of the currently running Gradle is in the Gradle home (typical of a wrapper execution context).
	 * If this isn't the case, we try to use the current Gradle home, if available, as the distribution.
	 * Finally, if nothing matches, we default to the system-wide Gradle distribution.
	 *
	 * @param gradle the {@link Gradle} instance of the build generating the the IDE files
	 * @return path to Gradle entry script to use within the generated IDE files
	 */
	public static String toGradleCommand(Gradle gradle) {
		java.util.Optional<String> gradleWrapperPath = java.util.Optional.empty();

		Project rootProject = gradle.getRootProject();
		String gradlewExtension = SystemUtils.IS_OS_WINDOWS ? ".bat" : "";
		File gradlewFile = rootProject.file("gradlew" + gradlewExtension);
		if (gradlewFile.exists()) {
			gradleWrapperPath = java.util.Optional.of(gradlewFile.getAbsolutePath());
		}

		if (gradle.getGradleHomeDir() != null) {
			if (gradleWrapperPath.isPresent() && gradle.getGradleHomeDir().getAbsolutePath().startsWith(gradle.getGradleUserHomeDir().getAbsolutePath())) {
				return gradleWrapperPath.get();
			}
			return gradle.getGradleHomeDir().getAbsolutePath() + "/bin/gradle";
		}

		return gradleWrapperPath.orElse("gradle");
	}
}
