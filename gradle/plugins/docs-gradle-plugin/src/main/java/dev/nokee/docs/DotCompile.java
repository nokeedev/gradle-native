package dev.nokee.docs;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class DotCompile extends DefaultTask {

	@Inject
	public DotCompile(ProjectLayout projectLayout) {
		getOutputDirectory().set(projectLayout.getBuildDirectory().dir("tmp/" + getName()));
		getOutputDirectory().disallowChanges();
	}

	@InputFiles
	public abstract ConfigurableFileCollection getSource();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	@TaskAction
	private void doCompile() {
		getSource().getFiles().forEach(it -> {
			CommandLine commandLine = CommandLine.parse("dot -Tpng " + it.getAbsolutePath() + " -o " + getOutputDirectory().file(it.getName()).get().getAsFile().getAbsolutePath().replace(".dot", ".png"));
			Executor executor = new DefaultExecutor();
			executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
			executor.setExitValue(0);
			try {
				executor.execute(commandLine);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
}
