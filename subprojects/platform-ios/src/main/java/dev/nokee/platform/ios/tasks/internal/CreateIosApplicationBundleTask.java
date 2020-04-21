package dev.nokee.platform.ios.tasks.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public abstract class CreateIosApplicationBundleTask extends DefaultTask {
	@OutputDirectory
	public abstract RegularFileProperty getApplicationBundle();

	@InputFiles
	public abstract ConfigurableFileCollection getSources();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@TaskAction
	private void create() {
		getFileOperations().sync(spec -> {
			spec.from(getSources().getFiles());
			spec.into(getApplicationBundle());
		});
	}
}
