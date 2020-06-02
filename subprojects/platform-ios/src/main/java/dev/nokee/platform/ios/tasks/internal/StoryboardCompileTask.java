package dev.nokee.platform.ios.tasks.internal;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.GradleWorkerExecutorEngine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileType;
import org.gradle.api.internal.tasks.TaskExecutionOutcome;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

@CacheableTask
public abstract class StoryboardCompileTask extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@Input
	public abstract Property<String> getModule();

	// TODO: This may need to be richer so we keep the context path
	@Incremental
	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract ConfigurableFileCollection getSources();

	@Nested
	public abstract Property<CommandLineTool> getInterfaceBuilderTool();

	@Inject
	protected abstract ObjectFactory getObjects();

	@TaskAction
	private void compile(InputChanges inputChanges) throws IOException {
		new File(getTemporaryDir(), "outputs.txt").delete();
		if (inputChanges.isIncremental()) {
			for (FileChange it : inputChanges.getFileChanges(getSources())) {
				if (it.getChangeType().equals(ChangeType.REMOVED)) {
					if (it.getFileType().equals(FileType.FILE)) {
						FileUtils.deleteDirectory(new File(getDestinationDirectory().get().getAsFile().getAbsolutePath() + "/" + it.getFile().getParentFile().getName() + "/" + it.getFile().getName() + "c"));
					} else if (it.getFileType().equals(FileType.DIRECTORY)) {
						FileUtils.deleteDirectory(new File(getDestinationDirectory().get().getAsFile().getAbsolutePath() + "/" + it.getFile().getName()));
					}
				} else {
					build(it.getFile());
				}
			}
		} else {
			if (getSources().getAsFileTree().isEmpty()) {
				getState().setDidWork(false);
				getState().setOutcome(TaskExecutionOutcome.NO_SOURCE);
				return;
			}
			for (File source : getSources()) {
				build(source);
			}
		}
	}

	private void build(File source) {
		getInterfaceBuilderTool().get()
			.withArguments(
				"--errors", "--warnings",
				"--notices",
				"--module", getModule().get(),
				"--output-partial-info-plist", getTemporaryDir().getAbsolutePath() + "/" + FilenameUtils.removeExtension(source.getName()) + "-SBPartialInfo.plist",
				"--auto-activate-custom-fonts",
				"--target-device", "iphone", "--target-device", "ipad",
				"--minimum-deployment-target", "13.2",
				"--output-format", "human-readable-text",
				"--compilation-directory", getDestinationDirectory().get().getAsFile().getAbsolutePath() + "/" + source.getParentFile().getName(),
				source.getAbsolutePath())
			.newInvocation()
			.appendStandardStreamToFile(new File(getTemporaryDir(), "outputs.txt"))
			.buildAndSubmit(getObjects().newInstance(GradleWorkerExecutorEngine.class));
	}
}
