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
public abstract class AsciicastCompile extends DefaultTask {
	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getAsciicastFile();

	@OutputFile
	public abstract RegularFileProperty getGifVideoFile();

	@Inject
	public AsciicastCompile() {
		getGifVideoFile().value(getLayout().getBuildDirectory().file(getAsciicastFile().map(it -> "tmp/" + getName() + "/" + FilenameUtils.removeExtension(it.getAsFile().getName()) + ".gif"))).disallowChanges();
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract WorkerExecutor getWorkerExecutor();

	@TaskAction
	private void doCompile() {
		getWorkerExecutor().classLoaderIsolation().submit(CompileAction.class, it -> {
			it.getInputFile().set(getAsciicastFile());
			it.getOutputFile().set(getGifVideoFile());
		});
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

			getExecOperations().exec(spec -> {
				// NOTE: Needs to quote the output path because the asciicast2git handle spaces in path when passed to gifsicle
				spec.commandLine("asciicast2gif", getParameters().getInputFile().get().getAsFile().getAbsolutePath(), "'" + outputFile.getAbsolutePath() + "'");
			});
		}

		@Inject
		protected abstract ExecOperations getExecOperations();
	}
}
