package dev.nokee.docs.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.OutputDirectory;

import javax.inject.Inject;

public abstract class ProcessorTask extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	public ProcessorTask() {
		getOutputDirectory().set(getProjectLayout().getBuildDirectory().dir("tmp/" + getName()));
		getOutputDirectory().disallowChanges();
	}

	@Inject
	protected abstract ProjectLayout getProjectLayout();
}
