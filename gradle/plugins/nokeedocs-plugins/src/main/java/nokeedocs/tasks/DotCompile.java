package nokeedocs.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileType;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.InputChanges;

import javax.inject.Inject;
import java.io.File;
import java.util.stream.StreamSupport;

@CacheableTask
public abstract class DotCompile extends DefaultTask {
	@PathSensitive(PathSensitivity.RELATIVE)
	@InputFiles
	@SkipWhenEmpty
	public abstract ConfigurableFileTree getSource();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

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

	@Inject
	protected abstract ExecOperations getExecOperations();

	private void compileFile(File sourceFile) {
		File outputFile = outputFileFor(sourceFile);
		outputFile.getParentFile().mkdirs();

		getExecOperations().exec(spec -> {
			spec.commandLine("dot", "-Tpng", sourceFile.getAbsolutePath(), "-o", outputFile.getAbsolutePath());
		});
	}

	private File outputFileFor(File file) {
		String relativePathFromSourceDirectory = getSource().getDir().toURI().relativize(file.toURI()).getPath().replace(".dot", ".png");
		return getOutputDirectory().file(relativePathFromSourceDirectory).get().getAsFile();
	}
}
