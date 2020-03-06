package dev.nokee.docs.tasks;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public abstract class GenerateSamplesContentTask extends ProcessorTask {
	private final List<Sample> samples = new ArrayList<>();

	@Nested
	protected List<Sample> getSamples() {
		return samples;
	}

	protected abstract ConfigurableFileCollection getSource();

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
		File outputFile = getOutputDirectory().get().file(sample.getPermalink().get() + "/index.adoc").getAsFile();
		File sourceFile = sample.getSourceDirectory().get().file("README.adoc").getAsFile();

		outputFile.getParentFile().mkdirs();

		try (OutputStream outStream = new FileOutputStream(outputFile)) {
			try (PrintWriter out = new PrintWriter(new CloseShieldOutputStream(outStream))) {
				writeAsciidoctorHeader(out, sample);
			}
			Files.copy(sourceFile.toPath(), outStream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void writeAsciidoctorHeader(PrintWriter out, Sample sample) {
		out.println(":jbake-permalink: " + sample.getPermalink().get());
		out.println(":jbake-archivebasename: " + sample.getArchiveBaseName().get());
		out.println(":includedir: " + sample.getSourceDirectory().get().getAsFile().getAbsolutePath());
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
