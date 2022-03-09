package dev.nokee.docs.tasks;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;

@CacheableTask
public abstract class GifCompile extends DefaultTask {
	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getGifVideoFile();

	@OutputFile
	public abstract RegularFileProperty getMp4VideoFile();

	@Inject
	public GifCompile() {
		getMp4VideoFile().value(getLayout().getBuildDirectory().file(getGifVideoFile().map(it -> "tmp/" + getName() + "/" + FilenameUtils.removeExtension(it.getAsFile().getName()) + ".mp4"))).disallowChanges();
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract WorkerExecutor getWorkerExecutor();

	@TaskAction
	private void doCompile() {
		getWorkerExecutor().classLoaderIsolation().submit(CompileAction.class, it -> {
			it.getInputFile().set(getGifVideoFile());
			it.getOutputFile().set(getMp4VideoFile());
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

			getExecOperations().exec(spec -> {
				spec.commandLine("ffmpeg", "-y", "-filter_threads", "1", "-loglevel", "fatal", "-f", "gif", "-i", getParameters().getInputFile().get().getAsFile().getAbsolutePath(), outputFile.getAbsolutePath());
			});
		}
	}
}
