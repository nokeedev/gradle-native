package dev.nokee.ide.xcode.internal.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

public abstract class SyncXcodeIdeProduct extends DefaultTask {
	@InputFiles
	public abstract Property<FileSystemLocation> getProductLocation();

	@Internal
	public abstract Property<FileSystemLocation> getDestinationLocation();

	@OutputFiles
	protected FileCollection getOutputFiles() {
		return getObjects().fileCollection().from(getDestinationLocation().map(location -> {
			File locationAsFile = location.getAsFile();
			if (locationAsFile.isDirectory()) {
				return getObjects().fileTree().setDir(locationAsFile).matching(it -> it.include("**/*"));
			}
			return locationAsFile;
		}));
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@TaskAction
	private void sync() throws IOException {
		// TODO: Investigate using APFS Clone Copy when syncing the product to Xcode built product directory.
		//       See https://eclecticlight.co/2020/04/14/copy-move-and-clone-files-in-apfs-a-primer/
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
