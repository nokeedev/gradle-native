package dev.nokee.docs.tasks;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileType;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.StreamSupport;

@CacheableTask
public abstract class ProcessAsciidoctor extends ProcessorTask {
	@Incremental
	@InputFiles
	public abstract ConfigurableFileTree getSource();

	@Input
	@Optional
	public abstract Property<String> getRelativePath();

	@Input
	public abstract Property<String> getVersion();

	@Input
	public abstract Property<String> getMinimumGradleVersion();

	@TaskAction
	private void doProcess(InputChanges inputChanges) {
		if (!inputChanges.isIncremental()) {
			getSource().forEach(this::processFile);
		} else {
			StreamSupport.stream(inputChanges.getFileChanges(getSource()).spliterator(), false).filter(ProcessAsciidoctor::notDirectoryChanges).forEach(this::processChanges);
		}
	}

	private static boolean notDirectoryChanges(FileChange fileChange) {
		return fileChange.getFileType() != FileType.DIRECTORY;
	}

	private void processChanges(FileChange fileChange) {
		File targetFile = getOutputDirectory().file(fileChange.getNormalizedPath()).get().getAsFile();
		if (fileChange.getChangeType() == ChangeType.REMOVED) {
			targetFile.delete();
		} else {
			processFile(fileChange.getFile());
		}
	}

	private void processFile(File sourceFile) {
		File outputFile = outputFileFor(sourceFile);
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

	private File outputFileFor(File file) {
		String relativePathFromSourceDirectory = getSource().getDir().toURI().relativize(file.toURI()).getPath();
		if (getRelativePath().isPresent()) {
			return getOutputDirectory().file(getRelativePath().get() + "/" + relativePathFromSourceDirectory).get().getAsFile();
		}
		return getOutputDirectory().file(relativePathFromSourceDirectory).get().getAsFile();
	}

	private void writeAsciidoctorHeader(PrintWriter out) {
		out.println(":jbake-version: " + getVersion().get());
		out.println(":toc:");
		out.println(":toclevels: 1");
		out.println(":toc-title: Contents");
		out.println(":icons: font");
		out.println(":idprefix:");
		out.println(":jbake-status: published");
		out.println(":encoding: utf-8");
		out.println(":lang: en-US");
		out.println(":sectanchors: true");
		out.println(":sectlinks: true");
		out.println(":linkattrs: true");

		// TODO: Make sure to sync the Gradle version
		out.println(":gradle-user-manual: https://docs.gradle.org/" + getMinimumGradleVersion().get() + "/userguide");
		out.println(":gradle-language-reference: https://docs.gradle.org/" + getMinimumGradleVersion().get() + "/dsl");
		out.println(":gradle-api-reference: https://docs.gradle.org/" + getMinimumGradleVersion().get() + "/javadoc");
		out.println(":gradle-guides: https://guides.gradle.org/");
	}
}
