package dev.nokee.ide.visualstudio.internal;

import org.gradle.api.Task;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskDependency;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

// TODO: Rename to ProjectReference and unpack the metadata
public class VisualStudioIdeProjectInformation implements TaskDependency {
	private final VisualStudioIdeProjectMetadata metadata;

	public VisualStudioIdeProjectInformation(VisualStudioIdeProjectMetadata metadata) {
		this.metadata = metadata;
	}

	@InputFile
	public File getProjectLocation() {
		return metadata.getFile();
	}

	@Internal
	public VisualStudioIdeGuid getProjectGuid() {
		return metadata.getProjectGuid();
	}

	@Override
	public Set<? extends Task> getDependencies(@Nullable Task task) {
		return metadata.getGeneratorTasks();
	}
}
