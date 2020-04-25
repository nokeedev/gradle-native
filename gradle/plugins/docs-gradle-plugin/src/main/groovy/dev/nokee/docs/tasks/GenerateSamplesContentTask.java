package dev.nokee.docs.tasks;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;

@CacheableTask
public abstract class GenerateSamplesContentTask extends ProcessorTask {
	@Inject
	protected abstract ObjectFactory getObjectFactory();

	@TaskAction
	private void doGenerate() {
		writeContent();
	}

	private void writeContent() {
		File outputFile = getOutputDirectory().get().file("index.adoc").getAsFile();
		File sourceFile = getSourceDirectory().get().getAsFile();

		outputFile.getParentFile().mkdirs();

		try (OutputStream outStream = new FileOutputStream(outputFile)) {
			try (PrintWriter out = new PrintWriter(new CloseShieldOutputStream(outStream))) {
				writeAsciidoctorHeader(out);
			}
			Files.copy(sourceFile.toPath(), outStream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void writeAsciidoctorHeader(PrintWriter out) {
		out.println(":jbake-permalink: " + getPermalink().get());
		out.println(":jbake-archivebasename: " + getArchiveBaseName().get());
		out.println(":includedir: .");
	}

	@PathSensitive(PathSensitivity.RELATIVE)
	@InputFile
	public abstract RegularFileProperty getSourceDirectory();

	@Input
	public abstract Property<String> getPermalink();

	@Input
	public abstract Property<String> getArchiveBaseName();
}
