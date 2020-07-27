package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdeTarget;
import dev.nokee.ide.visualstudio.internal.tasks.GenerateVisualStudioIdeProjectTask;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultVisualStudioIdeProject implements VisualStudioIdeProject, IdeProjectInternal {
	@Getter private final String name;
	@Getter private final TaskProvider<GenerateVisualStudioIdeProjectTask> generatorTask;
	@Getter private final List<DefaultVisualStudioIdeTarget> targets = new ArrayList<>();

	@Inject
	public DefaultVisualStudioIdeProject(String name) {
		this.name = name;
		generatorTask = getTasks().register(name + "VisualStudioProject", GenerateVisualStudioIdeProjectTask.class, this);
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public Provider<FileSystemLocation> getLocation() {
		return generatorTask.flatMap(GenerateVisualStudioIdeProjectTask::getProjectLocation);
	}

	public abstract ConfigurableFileCollection getSourceFiles();

	public abstract ConfigurableFileCollection getHeaderFiles();

	public abstract ConfigurableFileCollection getBuildFiles();

	public Provider<VisualStudioIdeGuid> getProjectGuid() {
		return getLocation().map(it -> VisualStudioIdeGuid.stableGuidFrom(it.getAsFile()));
	}

	@Override
	public void target(VisualStudioIdeProjectConfiguration projectConfiguration, Action<? super VisualStudioIdeTarget> action) {
		val target = targets.stream().filter(it -> it.getProjectConfiguration().equals(projectConfiguration)).findAny().orElseGet(() -> newTarget(projectConfiguration));
		action.execute(target);
	}

	private DefaultVisualStudioIdeTarget newTarget(VisualStudioIdeProjectConfiguration projectConfiguration) {
		val result = getObjects().newInstance(DefaultVisualStudioIdeTarget.class, projectConfiguration);
		targets.add(result);
		return result;
	}

	@Override
	public String getDisplayName() {
		return "Visual Studio project";
	}
}
