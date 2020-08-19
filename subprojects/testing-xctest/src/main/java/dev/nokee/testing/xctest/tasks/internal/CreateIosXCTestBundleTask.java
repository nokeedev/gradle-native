package dev.nokee.testing.xctest.tasks.internal;

import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class CreateIosXCTestBundleTask extends DefaultTask {
	private final Property<FileSystemLocation> xCTestBundle;
	private final ConfigurableFileCollection sources;
	@Getter(value=AccessLevel.PROTECTED, onMethod_={@Inject}) private final FileSystemOperations fileOperations;

	@OutputDirectory
	public Property<FileSystemLocation> getXCTestBundle() {
		return xCTestBundle;
	}

	@InputFiles
	public ConfigurableFileCollection getSources() {
		return sources;
	}

	@Inject
	public CreateIosXCTestBundleTask(ObjectFactory objects, FileSystemOperations fileOperations) {
		this.xCTestBundle = objects.property(FileSystemLocation.class);
		this.sources = objects.fileCollection();
		this.fileOperations = fileOperations;
	}

	@TaskAction
	private void create() {
		getFileOperations().sync(spec -> {
			spec.from(getSources().getFiles());
			spec.into(getXCTestBundle());
		});
	}
}
