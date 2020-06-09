package dev.nokee.ide.xcode.internal.tasks;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.internal.tasks.TaskExecutionOutcome;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
		File destinationLocation = getDestinationLocation().get().getAsFile();

		if (!productLocation.exists()) {
			if (destinationLocation.isDirectory()) {
				FileUtils.deleteDirectory(destinationLocation);
			} else if (destinationLocation.isFile()) {
				destinationLocation.delete();
			}
			return;
		}

		if (productLocation.isDirectory()) {
			getFileOperations().sync(spec -> {
				spec.from(getProductLocation());
				spec.into(destinationLocation);
			});
		} else {
			ignore(destinationLocation.delete());
			Files.copy(productLocation.toPath(), destinationLocation.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
		}
	}

	private static void ignore(Object o) {}
}
