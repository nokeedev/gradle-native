package dev.nokee.platform.ios.tasks.internal;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.GradleWorkerExecutorEngine;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class StoryboardLinkTask extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@Input
	public abstract Property<String> getModule();

	// TODO: This may need to be richer so we keep the context path
	@SkipWhenEmpty
	@InputFiles
	protected List<File> getInputFiles() {
		return getSources().getFiles().stream().flatMap(it -> {
			File[] files = it.listFiles();
			if (files == null) {
				return Stream.empty();
			}
			return Arrays.stream(files);
		}).collect(Collectors.toList());
	}

	@Internal
	public abstract ConfigurableFileCollection getSources();

	@Nested
	public abstract Property<CommandLineTool> getInterfaceBuilderTool();

	public StoryboardLinkTask() {
		dependsOn(getSources()); // TODO: Test dependencies are followed via the source
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@TaskAction
	private void doLink() {
		getInterfaceBuilderTool().get()
			.withArguments(
				"--errors", "--warnings", "--notices",
				"--module", getModule().get(),
				"--auto-activate-custom-fonts",
				"--target-device", "iphone", "--target-device", "ipad",
				"--minimum-deployment-target", "13.2",
				"--output-format", "human-readable-text",
				"--link", getDestinationDirectory().get().getAsFile().getAbsolutePath(), getInputFiles().stream().map(File::getAbsolutePath).collect(Collectors.joining(" ")))
			.newInvocation()
			.appendStandardStreamToFile(new File(getTemporaryDir(), "outputs.txt"))
			.buildAndSubmit(getObjects().newInstance(GradleWorkerExecutorEngine.class));
	}
}
