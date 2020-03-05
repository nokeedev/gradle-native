package dev.nokee.docs.tasks;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileType;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.InputChanges;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.StreamSupport;

public abstract class DotCompile extends ProcessorTask {
	@InputFiles
	public abstract ConfigurableFileTree getSource();

	@Input
	public abstract Property<String> getRelativePath();

	@TaskAction
	private void doCompile(InputChanges inputChanges) {
		if (!inputChanges.isIncremental()) {
			getSource().forEach(this::compileFile);
		} else {
			StreamSupport.stream(inputChanges.getFileChanges(getSource()).spliterator(), false).filter(DotCompile::notDirectoryChanges).forEach(this::processChanges);
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
			compileFile(fileChange.getFile());
		}
	}

	private void compileFile(File sourceFile) {
		File outputFile = outputFileFor(sourceFile);
		outputFile.getParentFile().mkdirs();

		CommandLine commandLine = CommandLine.parse("dot -Tpng " + sourceFile.getAbsolutePath() + " -o " + outputFile);
		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);
		try {
			executor.execute(commandLine);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private File outputFileFor(File file) {
		String relativePathFromSourceDirectory = getSource().getDir().toURI().relativize(file.toURI()).getPath().replace(".dot", ".png");
		return getOutputDirectory().file(getRelativePath().get() + "/" + relativePathFromSourceDirectory).get().getAsFile();
	}
}
