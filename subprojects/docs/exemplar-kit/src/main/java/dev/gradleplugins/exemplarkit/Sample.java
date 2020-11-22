package dev.gradleplugins.exemplarkit;

import dev.nokee.core.exec.CommandLineTool;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public abstract class Sample {
	public static Sample empty() {
		return new SampleEmptyImpl();
	}

	public static Sample fromDirectory(File sampleDirectory) {
		if (sampleDirectory.isDirectory()) {
			return new SampleDirectoryImpl(sampleDirectory);
		}
		throw new IllegalArgumentException(String.format("Please specify a valid directory because directory '%s' does not exists.", sampleDirectory.getAbsolutePath()));
	}

	public static Sample fromArchive(File sampleArchive) {
		if (sampleArchive.isFile()) {
			return new SampleArchiveImpl(sampleArchive);
		}
		throw new IllegalArgumentException(String.format("Please specify a valid archive because archive '%s' does not exists.", sampleArchive.getAbsolutePath()));
	}

	abstract void writeToDirectory(File directory);

	@EqualsAndHashCode(callSuper = false)
	private static final class SampleEmptyImpl extends Sample {
		@Override
		void writeToDirectory(File directory) {
			// Do nothing, it's empty
		}
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class SampleArchiveImpl extends Sample {
		private final File sampleArchiveFile;

		private SampleArchiveImpl(File sampleArchiveFile) {
			this.sampleArchiveFile = sampleArchiveFile;
		}

		@Override
		void writeToDirectory(File directory) {
			if (SystemUtils.IS_OS_UNIX) {
				CommandLineTool.fromPath("unzip")
					.get()
					.withArguments("-q", "-o", sampleArchiveFile.getAbsolutePath(), "-d", directory.getAbsolutePath())
					.execute()
					.waitFor()
					.assertNormalExitValue();
			} else {
				Expand unzip = new Expand();
				unzip.setSrc(sampleArchiveFile);
				unzip.setDest(directory);
				unzip.setProject(new Project());
				unzip.execute();
			}
		}
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class SampleDirectoryImpl extends Sample {
		private final File sampleDirectory;

		private SampleDirectoryImpl(File sampleDirectory) {
			this.sampleDirectory = sampleDirectory;
		}

		@Override
		void writeToDirectory(File directory) {
			try {
				copyDirectory(sampleDirectory.toPath(), directory.toPath());
			} catch (IOException e) {
				throw new UncheckedIOException(String.format("Could not copy sample from '%s' to '%s' because of an error.", sampleDirectory.getAbsolutePath(), directory.getAbsolutePath()), e);
			}
		}

		private static void copyDirectory(Path sourceDirectory, Path targetDirectory) throws IOException {
			Files.walk(sourceDirectory)
				.forEach(sourcePath -> {
					Path targetPath = targetDirectory.resolve(sourceDirectory.relativize(sourcePath));
					try {
						Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						throw new UncheckedIOException(String.format("Could not copy file from '%s' to '%s' because of an error.", sourcePath.toString(), targetPath.toString()), e);
					}
				});
		}
	}
}
