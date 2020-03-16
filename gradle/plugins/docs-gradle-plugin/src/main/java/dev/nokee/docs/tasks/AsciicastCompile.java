package dev.nokee.docs.tasks;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileType;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.StreamSupport;

public abstract class AsciicastCompile extends ProcessorTask {
	@InputFiles
	public abstract ConfigurableFileTree getSource();

	@TaskAction
	private void doCompile(InputChanges inputChanges) {
		if (!inputChanges.isIncremental()) {
			getSource().forEach(this::submitCompileFile);
		} else {
			StreamSupport.stream(inputChanges.getFileChanges(getSource()).spliterator(), false).filter(AsciicastCompile::notDirectoryChanges).forEach(this::processChanges);
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
			submitCompileFile(fileChange.getFile());
		}
	}

	@Inject
	protected abstract WorkerExecutor getWorkerExecutor();

//	private void compileFile(File sourceFile) {
//		File outputFile = outputFileFor(sourceFile);
//		outputFile.getParentFile().mkdirs();
//
//		CommandLine commandLine = CommandLine.parse("asciicast2gif " + sourceFile.getAbsolutePath() + " " + outputFile);
//		Executor executor = new DefaultExecutor();
//		// TODO: Pump stream to file
//		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
//		executor.setExitValue(0);
//		try {
//			executor.execute(commandLine);
//		} catch (IOException e) {
//			throw new UncheckedIOException(e);
//		}
//	}

	private void submitCompileFile(File sourceFile) {
		getWorkerExecutor().classLoaderIsolation().submit(CompileAction.class, it -> {
			it.getInputFile().set(sourceFile);
			it.getOutputFile().set(outputFileFor(sourceFile));
		});
	}

	private File outputFileFor(File file) {
		String relativePathFromSourceDirectory = getSource().getDir().toURI().relativize(file.toURI()).getPath().replace(".cast", ".gif");
		return getOutputDirectory().file(relativePathFromSourceDirectory).get().getAsFile();
	}

	public interface CompileParameters extends WorkParameters {
		RegularFileProperty getInputFile();
		RegularFileProperty getOutputFile();
	}

	public static abstract class CompileAction implements WorkAction<CompileParameters> {
		@Override
		public void execute() {
			File outputFile = getParameters().getOutputFile().get().getAsFile();
			outputFile.getParentFile().mkdirs();

			CommandLine commandLine = CommandLine.parse("asciicast2gif " + getParameters().getInputFile().get().getAsFile().getAbsolutePath() + " " + outputFile.getAbsolutePath());
			Executor executor = new DefaultExecutor();
			// TODO: Pump stream to file
			executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
			executor.setExitValue(0);
			try {
				executor.execute(commandLine);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
