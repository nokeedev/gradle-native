package dev.gradleplugins.documentationkit.site.githubpages.tasks;

import dev.gradleplugins.documentationkit.site.githubpages.GitHubPagesCustomDomain;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Generate a CNAME file for the specified custom domain.
 */
public abstract class GenerateGitHubPagesCustomDomainCanonicalNameRecord extends DefaultTask {
	@Input
	@Optional
	public abstract Property<GitHubPagesCustomDomain> getCustomDomain();

	@OutputFile
	public abstract RegularFileProperty getOutputFile();

	@TaskAction
	private void doGenerate() throws IOException {
		if (!getCustomDomain().isPresent()) {
			setDidWork(false);
			return;
		}

		FileUtils.write(getOutputFile().get().getAsFile(), getCustomDomain().get().get(), Charset.defaultCharset());
	}
}
