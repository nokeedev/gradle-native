package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.visualstudio.VisualStudioIdeGuid;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdeTarget;
import dev.nokee.ide.visualstudio.internal.tasks.GenerateVisualStudioIdeProjectTask;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract class DefaultVisualStudioIdeProject implements VisualStudioIdeProject, IdeProjectInternal {
	@Getter(onMethod_={@Internal}) private final String name;
	@Getter(onMethod_={@Internal}) private final TaskProvider<GenerateVisualStudioIdeProjectTask> generatorTask;

	@Inject
	public DefaultVisualStudioIdeProject(String name) {
		this.name = name;
		generatorTask = getTasks().register(name + "VisualStudioProject", GenerateVisualStudioIdeProjectTask.class, this);
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Internal
	@Override
	public Provider<FileSystemLocation> getLocation() {
		return generatorTask.flatMap(GenerateVisualStudioIdeProjectTask::getProjectLocation);
	}

	@InputFile
	@Override
	public Provider<FileSystemLocation> getProjectLocation() {
		return getLocation();
	}

	@Internal
	public abstract ConfigurableFileCollection getSourceFiles();

	@Internal
	public abstract ConfigurableFileCollection getHeaderFiles();

	@Internal
	public abstract ConfigurableFileCollection getBuildFiles();

	@Internal
	public Provider<VisualStudioIdeGuid> getProjectGuid() {
		return getLocation().map(it -> DefaultVisualStudioIdeGuid.stableGuidFrom(it.getAsFile()));
	}

	@Internal
	public abstract DomainObjectSet<VisualStudioIdeTarget> getTargets();

	@Override
	public void target(VisualStudioIdeProjectConfiguration projectConfiguration, Action<? super VisualStudioIdeTarget> action) {
		val target = getTargets().stream().filter(it -> it.getProjectConfiguration().equals(projectConfiguration)).findAny().orElseGet(() -> newTarget(projectConfiguration));
		action.execute(target);
	}

	private DefaultVisualStudioIdeTarget newTarget(VisualStudioIdeProjectConfiguration projectConfiguration) {
		val result = getObjects().newInstance(DefaultVisualStudioIdeTarget.class, projectConfiguration);
		getTargets().add(result);
		return result;
	}

	@Internal
	@Override
	public String getDisplayName() {
		return "Visual Studio project";
	}
}
