package dev.nokee.docs;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class GenerateSamplesContentTask extends DefaultTask {
	private final List<Sample> samples = new ArrayList<>();

	@Inject
	public GenerateSamplesContentTask(ProjectLayout projectLayout) {
		getOutputDirectory().set(projectLayout.getBuildDirectory().dir("tmp/" + getName()));
		getOutputDirectory().disallowChanges();
	}

	@Nested
	protected List<Sample> getSamples() {
		return samples;
	}

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	public void sample(Action<? super Sample> action) {
		Sample result = getObjectFactory().newInstance(Sample.class);
		action.execute(result);
		samples.add(result);
	}

	@Inject
	protected abstract ObjectFactory getObjectFactory();

	@TaskAction
	private void doGenerate() {
		samples.forEach(this::writeContent);
	}

	private void writeContent(Sample sample) {
		File outputFile = getOutputDirectory().get().dir(sample.getPermalink().get() + "/index.adoc").getAsFile();
		Path sourceFile = sample.getSourceDirectory().get().file("README.adoc").getAsFile().toPath();

		outputFile.getParentFile().mkdirs();

		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
			bos.write(toJBakeContentHeader(sample));
			Files.copy(sourceFile, bos);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private byte[] toJBakeContentHeader(Sample sample) {
		StringBuilder sb = new StringBuilder();
		sb.append(":jbake-version: " + sample.getVersion().get()).append("\n");
		sb.append(":jbake-permalink: " + sample.getPermalink().get()).append("\n");
		sb.append(":jbake-archivebasename: " + sample.getArchiveBaseName().get()).append("\n");
		sb.append(":includedir: " + sample.getSourceDirectory().get().getAsFile().getAbsolutePath()).append("\n");

		// TODO: Make sure to sync the Gradle version
		sb.append(":gradle-user-manual: https://docs.gradle.org/6.2.1/userguide").append("\n");
		sb.append(":gradle-language-reference: https://docs.gradle.org/6.2.1/dsl").append("\n");
		sb.append(":gradle-api-reference: https://docs.gradle.org/6.2.1/javadoc").append("\n");
		sb.append(":gradle-guides: https://guides.gradle.org/").append("\n");
		return sb.toString().getBytes();
	}

	public interface Sample {
		@InputDirectory
		DirectoryProperty getSourceDirectory();

		@Input
		Property<String> getPermalink();

		@Input
		Property<String> getVersion();

		@Input
		Property<String> getArchiveBaseName();
	}
}
