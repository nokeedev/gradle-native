package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.internal.tasks.GenerateVisualStudioIdeProjectTask;
import lombok.Getter;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.UUID;

public abstract class DefaultVisualStudioIdeProject implements VisualStudioIdeProject, IdeProjectInternal {
	@Getter private final String name;
	@Getter private final TaskProvider<GenerateVisualStudioIdeProjectTask> generatorTask;

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

	public Provider<UUID> getProjectGuid() {
		return getLocation().map(it -> UUID.nameUUIDFromBytes(it.getAsFile().getAbsolutePath().getBytes()));
	}

	@Override
	public String getDisplayName() {
		return "Visual Studio project";
	}
}
