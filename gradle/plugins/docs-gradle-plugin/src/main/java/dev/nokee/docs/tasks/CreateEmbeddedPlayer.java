package dev.nokee.docs.tasks;

import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileType;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.*;
import java.util.stream.StreamSupport;

@CacheableTask
public abstract class CreateEmbeddedPlayer extends ProcessorTask {
	@PathSensitive(PathSensitivity.RELATIVE)
	@InputFiles
	public abstract ConfigurableFileTree getSource();

	@TaskAction
	private void doCompile(InputChanges inputChanges) {
		if (!inputChanges.isIncremental()) {
			getSource().forEach(this::submitCompileFile);
		} else {
			StreamSupport.stream(inputChanges.getFileChanges(getSource()).spliterator(), false).filter(CreateEmbeddedPlayer::notDirectoryChanges).forEach(this::processChanges);
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
		String relativePathFromSourceDirectory = getSource().getDir().toURI().relativize(file.toURI()).getPath().replace(".mp4", ".embed.html");
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

			try (PrintWriter out = new PrintWriter(new FileOutputStream(outputFile))) {
				out.println("<!DOCTYPE html>");
				out.println("<html>");
				out.println("<head>");
				out.println("	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
				out.println("	<style type=\"text/css\">");
				out.println("		body {");
				out.println("			background-color: black;");
				out.println("		}");
				out.println("		video {");
				out.println("			width:100%;");
				out.println("			max-width:600px;");
				out.println("			height:auto;");
				out.println("		}");
				out.println("	</style>");
				out.println("</head>");
				out.println("<body>");
				out.println("<video controls>");
				out.println("	<source src=\"" + getParameters().getInputFile().get().getAsFile().getName() + "\" type=\"video/mp4\">");
				out.println("Your browser does not support video");
				out.println("</video>");
				out.println("</body>");
				out.println("</html>");
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
