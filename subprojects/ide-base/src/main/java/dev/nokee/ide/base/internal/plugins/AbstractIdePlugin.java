package dev.nokee.ide.base.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.ide.base.IdeProject;
import dev.nokee.ide.base.internal.IdeProjectExtension;
import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.*;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Cast;
import org.gradle.internal.logging.ConsoleRenderer;
import org.gradle.plugins.ide.internal.IdeArtifactRegistry;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;
import org.gradle.process.ExecOperations;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.utils.DeferredUtils.realize;
import static dev.nokee.utils.ProjectUtils.isRootProject;

public abstract class AbstractIdePlugin<T extends IdeProject> implements Plugin<Project>, Describable {
	public static final String IDE_GROUP_NAME = "IDE";
	@Getter private TaskProvider<Delete> cleanTask;
	@Getter private TaskProvider<Task> lifecycleTask;

	@Override
	public final void apply(Project project) {
		this.project = project;
		val projectExtension = registerExtension(project);
		val workspaceExtension = asWorkspaceExtensionIfAvailable(projectExtension);

		// Configure clean task
		cleanTask = getTasks().register(getTaskName("clean"), Delete.class, task -> {
			task.setGroup(IDE_GROUP_NAME);
			task.setDescription("Cleans " + getDisplayName() + " configuration");
			task.delete(getProviders().provider(() -> projectExtension.getProjects().stream().map(IdeProject::getLocation).collect(Collectors.toList())));
			workspaceExtension.ifPresent(extension -> {
				task.delete(extension.getWorkspace().getLocation());
			});
		});
		projectExtension.getProjects().configureEach(ideProject -> {
			((IdeProjectInternal)ideProject).getGeneratorTask().configure(task -> task.shouldRunAfter(cleanTask));
		});
		workspaceExtension.ifPresent(extension -> extension.getWorkspace().getGeneratorTask().configure(task -> task.shouldRunAfter(cleanTask)));

		lifecycleTask = getTasks().register(getLifecycleTaskName(), task -> {
			task.setGroup(IDE_GROUP_NAME);
			task.setDescription("Generates " + getDisplayName() + " configuration");
			task.dependsOn(projectExtension.getProjects());
			task.shouldRunAfter(cleanTask);
			workspaceExtension.ifPresent(extension -> {
				task.dependsOn(extension.getWorkspace().getGeneratorTask());
				task.doLast(new Action<Task>() {
					@Override
					public void execute(Task task) {
						task.getLogger().lifecycle(String.format("Generated %s at %s", extension.getWorkspace().getDisplayName(), new ConsoleRenderer().asClickableFileUrl(extension.getWorkspace().getLocation().get().getAsFile())));
					}
				});
			});
		});

		// Since all Xcode components are expected to be registered lazily, we can't register the IDE project inside the configuration action above.
		// Instead, we rely on the schema after the project is evaluated and register metadata using the provider.
		// For better laziness, we should disallow all eager method from the containers (aka using a custom container).
		// We should also disallow any modification after we read the collection schema.
		project.afterEvaluate(proj -> {
			projectExtension.getProjects().getCollectionSchema().getElements().forEach(element -> {
				maybeRegisterIdeProject(element.getName());
			});
		});

		workspaceExtension.ifPresent(extension -> {
			extension.getWorkspace().getProjects().set(extension.getProjects());

			// Open configuration
			getTasks().register(getTaskName("open"), task -> {
				task.dependsOn(getLifecycleTask());
				task.setGroup(IDE_GROUP_NAME);
				task.setDescription("Opens the " + extension.getWorkspace().getDisplayName());
				task.doLast(new Action<Task>() {
					@Override
					public void execute(Task task) {
						if (SystemUtils.IS_OS_MAC) {
							getExecOperations().exec(spec -> spec.commandLine("open", extension.getWorkspace().getLocation().get()));
						} else {
							try {
								Desktop.getDesktop().open(extension.getWorkspace().getLocation().get().getAsFile());
							} catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						}
					}
				});
			});
		});

		doProjectApply(projectExtension);
		workspaceExtension.ifPresent(this::doWorkspaceApply);
	}

	protected abstract void doProjectApply(IdeProjectExtension<T> extension);

	protected abstract void doWorkspaceApply(IdeWorkspaceExtension<T> extension);

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ExecOperations getExecOperations();

	@Inject
	protected abstract IdeArtifactRegistry getArtifactRegistry();

	@Getter(AccessLevel.PROTECTED)
	private Project project;

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProjectLayout getLayout();

	private final Set<String> registeredIdeProjects = new HashSet<>();
	protected void registerIdeProject(String name) {
		getArtifactRegistry().registerIdeProject(newIdeProjectMetadata(getProviders().provider(() -> {
			realize(getProjectExtension().getProjects());
			return (IdeProjectInternal) getProjectExtension().getProjects().getByName(name);
		})));
		registeredIdeProjects.add(name);
	}

	private void maybeRegisterIdeProject(String name) {
		if (!registeredIdeProjects.contains(name)) {
			registerIdeProject(name);
		}
	}

	private IdeProjectExtension<T> getProjectExtension() {
		return (IdeProjectExtension<T>) getProject().getExtensions().getByName(getExtensionName());
	}

	//region Xcode IDE extension registration
	private IdeProjectExtension<T> registerExtension(Project project) {
		if (isRootProject(project)) {
			return registerWorkspaceExtension(project);
		}
		return registerProjectExtension(project);
	}

	private IdeWorkspaceExtension<T> registerWorkspaceExtension(Project project) {
		IdeWorkspaceExtension<T> extension = newIdeWorkspaceExtension();
		project.getExtensions().add(publicTypeOf(extension), getLifecycleTaskName(), extension);
		return extension;
	}

	protected abstract IdeWorkspaceExtension<T> newIdeWorkspaceExtension();

	private IdeProjectExtension<T> registerProjectExtension(Project project) {
		IdeProjectExtension<T> extension = newIdeProjectExtension();
		project.getExtensions().add(publicTypeOf(extension), getLifecycleTaskName(), extension);
		return extension;
	}

	protected abstract IdeProjectExtension<T> newIdeProjectExtension();

	private static TypeOf<Object> publicTypeOf(Object extension) {
		if (extension instanceof HasPublicType) {
			// TODO: Use nokee cast
			return Cast.uncheckedCast(((HasPublicType) extension).getPublicType());
		}
		return TypeOf.<Object>typeOf(extension.getClass());
	}

	private static <T extends IdeProject> Optional<IdeWorkspaceExtension<T>> asWorkspaceExtensionIfAvailable(IdeProjectExtension<T> projectExtension) {
		if (projectExtension instanceof IdeWorkspaceExtension) {
			return Optional.of((IdeWorkspaceExtension<T>) projectExtension);
		}
		return Optional.empty();
	}
	//endregion

	protected abstract IdeProjectMetadata newIdeProjectMetadata(Provider<IdeProjectInternal> ideProject);

	private String getTaskName(String verb) {
		return verb + StringUtils.capitalize(getLifecycleTaskName());
	}

	protected abstract String getExtensionName();

	protected String getLifecycleTaskName() {
		return getExtensionName();
	}

	@Override
	public String getDisplayName() {
		return Arrays.stream(GUtil.toWords(getExtensionName()).split(" ")).map(StringUtils::capitalize).collect(Collectors.joining(" ")) + " IDE";
	}

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

	// TODO: Implicit init script should probably also be added to make the user realize those are affecting your build.
	// TODO: Implicit gradle.properties should probably also be added to the build
	// NOTE: For the implicit files, we have to ensure it's obvious the files are not part of the build but part of the machine but affecting the build
	// CAUTION: The implicit gradle.properties is often use for storing credentials, we should be careful with that file.
	protected List<File> getBuildFiles() {
		ImmutableList.Builder<File> result = ImmutableList.builder();
		if (project.getBuildFile().exists()) {
			result.add(project.getBuildFile());
		}

		if (isRootProject(project)) {
			if (project.getGradle().getStartParameter().getSettingsFile() != null) {
				result.add(project.getGradle().getStartParameter().getSettingsFile());
			} else if (project.file("settings.gradle").exists()) {
				result.add(project.file("settings.gradle"));
			} else if (project.file("settings.gradle.kts").exists()) {
				result.add(project.file("settings.gradle.kts"));
			}

			if (project.file("gradle.properties").exists()) {
				result.add(project.file("gradle.properties"));
			}

			project.getGradle().getStartParameter().getInitScripts().forEach(result::add);
		}
		return result.build();
	}

	protected List<String> getAdditionalBuildArguments() {
		ImmutableList.Builder<String> result = ImmutableList.builder();
		project.getGradle().getStartParameter().getInitScripts().forEach(initScriptFile -> {
			result.add("--init-script", quote(initScriptFile.getAbsolutePath()));
		});
		return result.build();
	}

	private static String quote(String value) {
		return "\"" + value + "\"";
	}
}
