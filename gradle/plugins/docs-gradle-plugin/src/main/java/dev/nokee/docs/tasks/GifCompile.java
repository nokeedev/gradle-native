package dev.nokee.docs.tasks;

import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileType;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.util.stream.StreamSupport;

@CacheableTask
public abstract class GifCompile extends ProcessorTask {
	@PathSensitive(PathSensitivity.RELATIVE)
	@InputFiles
	public abstract ConfigurableFileTree getSource();

	@TaskAction
	private void doCompile(InputChanges inputChanges) {
		if (!inputChanges.isIncremental()) {
			getSource().forEach(this::submitCompileFile);
		} else {
			StreamSupport.stream(inputChanges.getFileChanges(getSource()).spliterator(), false).filter(GifCompile::notDirectoryChanges).forEach(this::processChanges);
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

	private void submitCompileFile(File sourceFile) {
		getWorkerExecutor().classLoaderIsolation().submit(CompileAction.class, it -> {
			it.getInputFile().set(sourceFile);
			it.getOutputFile().set(outputFileFor(sourceFile));
		});
	}

	private File outputFileFor(File file) {
		String relativePathFromSourceDirectory = getSource().getDir().toURI().relativize(file.toURI()).getPath().replace(".gif", ".mp4");
		return getOutputDirectory().file(relativePathFromSourceDirectory).get().getAsFile();
	}

	public interface CompileParameters extends WorkParameters {
		RegularFileProperty getInputFile();
		RegularFileProperty getOutputFile();
	}

	public static abstract class CompileAction implements WorkAction<CompileParameters> {
		@Inject
		protected abstract ExecOperations getExecOperations();

		@Override
		public void execute() {
			File outputFile = getParameters().getOutputFile().get().getAsFile();
			outputFile.getParentFile().mkdirs();

			getExecOperations().exec(spec -> {
				spec.commandLine("ffmpeg", "-f", "gif", "-i", getParameters().getInputFile().get().getAsFile().getAbsolutePath(), outputFile.getAbsolutePath());
			});
		}
	}
}
