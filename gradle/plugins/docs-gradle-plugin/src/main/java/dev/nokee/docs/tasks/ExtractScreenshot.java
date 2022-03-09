package dev.nokee.docs.tasks;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;

@CacheableTask
public abstract class ExtractScreenshot extends DefaultTask {
	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getMp4VideoFile();

	@OutputFile
	public abstract RegularFileProperty getScreenshotFile();

	@Inject
	public ExtractScreenshot() {
		getScreenshotFile().value(getLayout().getBuildDirectory().file(getMp4VideoFile().map(it -> "tmp/" + getName() + "/" + FilenameUtils.removeExtension(it.getAsFile().getName()) + ".png"))).disallowChanges();
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract WorkerExecutor getWorkerExecutor();

	@TaskAction
	private void doCompile(InputChanges inputChanges) {
		getWorkerExecutor().classLoaderIsolation().submit(CompileAction.class, it -> {
			it.getInputFile().set(getMp4VideoFile());
			it.getOutputFile().set(getScreenshotFile());
		});
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

			double videoLength = getVideoLength(getParameters().getInputFile().get().getAsFile());

			getExecOperations().exec(spec -> {
				spec.commandLine("ffmpeg", "-ss", (videoLength / 2), "-i", getParameters().getInputFile().get().getAsFile().getAbsolutePath(), "-frames:v", "1", outputFile.getAbsolutePath());
			});
		}

		public double getVideoLength(File file) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			getExecOperations().exec(spec -> {
				spec.commandLine("ffprobe", "-loglevel", "error", "-of", "csv=p=0", "-show_entries", "format=duration", file.getAbsolutePath());
				spec.setStandardOutput(outStream);
			});

			return Double.parseDouble(outStream.toString().trim());
		}
	}
}
