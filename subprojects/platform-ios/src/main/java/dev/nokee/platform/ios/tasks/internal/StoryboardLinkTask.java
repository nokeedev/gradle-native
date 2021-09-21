/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class StoryboardLinkTask extends DefaultTask {
	private final DirectoryProperty destinationDirectory;
	private final Property<String> module;
	private final ConfigurableFileCollection sources;
	private final Property<CommandLineTool> interfaceBuilderTool;
	private final ObjectFactory objects;

	@OutputDirectory
	public DirectoryProperty getDestinationDirectory() {
		return destinationDirectory;
	}

	@Input
	public Property<String> getModule() {
		return module;
	}

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
	public ConfigurableFileCollection getSources() {
		return sources;
	}

	@Nested
	public Property<CommandLineTool> getInterfaceBuilderTool() {
		return interfaceBuilderTool;
	}

	@Inject
	public StoryboardLinkTask(ObjectFactory objects) {
		this.destinationDirectory = objects.directoryProperty();
		this.module = objects.property(String.class);
		this.sources = objects.fileCollection();
		this.interfaceBuilderTool = objects.property(CommandLineTool.class);
		this.objects = objects;
		dependsOn(getSources()); // TODO: Test dependencies are followed via the source
	}

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
			.buildAndSubmit(objects.newInstance(GradleWorkerExecutorEngine.class));
	}
}
