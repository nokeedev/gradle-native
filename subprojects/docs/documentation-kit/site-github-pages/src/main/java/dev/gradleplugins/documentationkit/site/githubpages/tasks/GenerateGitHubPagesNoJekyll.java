package dev.gradleplugins.documentationkit.site.githubpages.tasks;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;

public abstract class GenerateGitHubPagesNoJekyll extends DefaultTask {
	@OutputFile
	public abstract RegularFileProperty getOutputFile();

	@Inject
	protected abstract ProjectLayout getLayout();

	public GenerateGitHubPagesNoJekyll() {
		getOutputFile().value(getLayout().getBuildDirectory().file("tmp/" + getName() + "/.nojekyll")).disallowChanges();
	}

	@TaskAction
	private void doGenerate() throws IOException {
		FileUtils.touch(getOutputFile().get().getAsFile());
	}
}
