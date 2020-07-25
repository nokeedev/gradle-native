package dev.nokee.ide.visualstudio.internal;

import lombok.val;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class VisualStudioIdeUtils {
	private static final Logger LOGGER = Logging.getLogger(VisualStudioIdeUtils.class);
	private VisualStudioIdeUtils() {}

	public static boolean isSolutionCurrentlyOpened(File solutionFile) {
		val dotvsDirectory = new File(solutionFile.getParentFile(), ".vs");

		if (!dotvsDirectory.exists()) {
			return false;
		}

		try {
			val visitor = new DotvsFileVisitor();
			Files.walkFileTree(dotvsDirectory.toPath(), visitor);
			return visitor.hasLockedFiles;
		} catch (IOException ex) {
			LOGGER.log(LogLevel.INFO, String.format("Unexpected exception encounted while verifying the solution '%s' wasn't held open by Visual Studio IDE.", solutionFile.getAbsolutePath()), ex);
			return true;
		}
	}

	private static class DotvsFileVisitor extends SimpleFileVisitor<Path> {
		private boolean hasLockedFiles = false;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			if (isLocked(file)) {
				hasLockedFiles = true;
				return FileVisitResult.TERMINATE;
			}
			return FileVisitResult.CONTINUE;
		}
	}

	private static boolean isLocked(Path path) {
		try (val inStream = new RandomAccessFile(path.toFile(), "rw")) {
			try (val channel = inStream.getChannel()) {
				try (val lock = channel.tryLock()) {
					return lock == null;
				}
			}
		} catch (Exception ex) {
			return true;
		}
	}
}
