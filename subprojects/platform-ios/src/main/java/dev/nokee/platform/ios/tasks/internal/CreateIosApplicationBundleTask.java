package dev.nokee.platform.ios.tasks.internal;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class CreateIosApplicationBundleTask extends DefaultTask {
	@OutputDirectory
	public abstract Property<FileSystemLocation> getApplicationBundle();

	@InputFiles
	public abstract ConfigurableFileCollection getSources();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@TaskAction
	private void create() throws IOException {
		getFileOperations().sync(spec -> {
			spec.from(getSources().getFiles());
			spec.into(getApplicationBundle());
		});

		// Oversimplification of how this file is created
		FileUtils.write(new File(getApplicationBundle().get().getAsFile(), "PkgInfo"), "APPL????", Charset.defaultCharset());
	}
}
