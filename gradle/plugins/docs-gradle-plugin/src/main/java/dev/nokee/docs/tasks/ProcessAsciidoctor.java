package dev.nokee.docs.tasks;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileType;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.*;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.StreamSupport;

@CacheableTask
public abstract class ProcessAsciidoctor extends ProcessorTask {
	@PathSensitive(PathSensitivity.RELATIVE)
	@Incremental
	@InputFiles
	public abstract ConfigurableFileTree getSource();

	@Input
	public abstract MapProperty<String, String> getAttributes();

	@TaskAction
	private void doProcess(InputChanges inputChanges) {
		if (!inputChanges.isIncremental()) {
			getSource().forEach(this::processFile);
		} else {
			StreamSupport.stream(inputChanges.getFileChanges(getSource()).spliterator(), false).filter(ProcessAsciidoctor::notDirectoryChanges).forEach(this::processChanges);
		}
	}

	private static boolean notDirectoryChanges(FileChange fileChange) {
		return fileChange.getFileType() != FileType.DIRECTORY;
	}

	private void processChanges(FileChange fileChange) {
		File targetFile = getOutputDirectory().file(fileChange.getNormalizedPath()).get().getAsFile();
		if (fileChange.getChangeType() == ChangeType.REMOVED) {
			targetFile.delete();
		} else {
			processFile(fileChange.getFile());
		}
	}

	private void processFile(File sourceFile) {
		File outputFile = outputFileFor(sourceFile);
		outputFile.getParentFile().mkdirs();

		try (OutputStream outStream = new FileOutputStream(outputFile)) {
			try (PrintWriter out = new PrintWriter(new CloseShieldOutputStream(outStream))) {
				writeAsciidoctorHeader(out);
			}
			Files.copy(sourceFile.toPath(), outStream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private File outputFileFor(File file) {
		String relativePathFromSourceDirectory = getSource().getDir().toURI().relativize(file.toURI()).getPath();
		return getOutputDirectory().file(relativePathFromSourceDirectory).get().getAsFile();
	}

	private void writeAsciidoctorHeader(PrintWriter out) {
		getAttributes().get().entrySet().forEach(entry -> {
			if (entry.getValue().isEmpty()) {
				out.println(String.format(":%s:", entry.getKey()));
			} else {
				out.println(String.format(":%s: %s", entry.getKey(), entry.getValue()));
			}
		});
	}
}
