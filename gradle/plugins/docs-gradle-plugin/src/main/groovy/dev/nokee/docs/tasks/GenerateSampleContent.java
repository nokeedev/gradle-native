package dev.nokee.docs.tasks;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.sources.SourceElement;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class GenerateSampleContent extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	@Internal
	public abstract Property<SourceElement> getTemplate();

	@Input
	@Optional
	protected String getTemplateClass() {
		if (getTemplate().isPresent()) {
			return getTemplate().get().getClass().getCanonicalName();
		}
		return null;
	}

	@Inject
	public GenerateSampleContent() {
		getOutputDirectory().value(getProjectLayout().getBuildDirectory().dir("tmp/" + getName())).disallowChanges();

		getOutputs().cacheIf(Specs.satisfyAll());
		onlyIf(it -> getTemplate().isPresent());
	}

	@Inject
	protected abstract ProjectLayout getProjectLayout();

	@TaskAction
	private void doGenerate() {
		try {
			FileUtils.deleteDirectory(getOutputDirectory().get().getAsFile());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		getTemplate().get().writeToProject(TestFile.of(getOutputDirectory().get().getAsFile()));
	}
}
