package dev.nokee.ide.xcode.internal.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public abstract class SyncXcodeIdeProduct extends DefaultTask {
	@InputFiles
	public abstract Property<FileSystemLocation> getProductLocation();

	@OutputFiles
	public abstract Property<FileSystemLocation> getDestinationLocation();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@TaskAction
	private void sync() throws IOException {
		File productLocation = getProductLocation().get().getAsFile();
		if (productLocation.isDirectory()) {
			getFileOperations().sync(spec -> {
				spec.from(getProductLocation());
				spec.into(getDestinationLocation());
			});
		} else {
			FileUtils.copyFile(productLocation, getDestinationLocation().get().getAsFile());
		}
	}
}
