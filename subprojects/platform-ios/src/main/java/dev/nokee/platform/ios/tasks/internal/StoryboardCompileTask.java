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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileType;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

@CacheableTask
public class StoryboardCompileTask extends DefaultTask {
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
	@Incremental
	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	public ConfigurableFileCollection getSources() {
		return sources;
	}

	@Nested
	public Property<CommandLineTool> getInterfaceBuilderTool() {
		return interfaceBuilderTool;
	}

	@Inject
	public StoryboardCompileTask(ObjectFactory objects) {
		this.destinationDirectory = objects.directoryProperty();
		this.module = objects.property(String.class);
		this.sources = objects.fileCollection();
		this.interfaceBuilderTool = objects.property(CommandLineTool.class);
		this.objects = objects;
	}

	@TaskAction
	private void compile(InputChanges inputChanges) throws IOException {
		new File(getTemporaryDir(), "outputs.txt").delete();
		if (inputChanges.isIncremental()) {
			for (FileChange it : inputChanges.getFileChanges(getSources())) {
				if (it.getChangeType().equals(ChangeType.REMOVED)) {
					if (it.getFileType().equals(FileType.FILE)) {
						FileUtils.deleteDirectory(new File(getDestinationDirectory().get().getAsFile().getAbsolutePath() + "/" + it.getFile().getParentFile().getName() + "/" + it.getFile().getName() + "c"));
					} else if (it.getFileType().equals(FileType.DIRECTORY)) {
						FileUtils.deleteDirectory(new File(getDestinationDirectory().get().getAsFile().getAbsolutePath() + "/" + it.getFile().getName()));
					}
				} else {
					build(it.getFile());
				}
			}
		} else {
			if (getSources().getAsFileTree().isEmpty()) {
				getState().setDidWork(false);
				return;
			}
			for (File source : getSources()) {
				build(source);
			}
		}
	}

	private void build(File source) {
		getInterfaceBuilderTool().get()
			.withArguments(
				"--errors", "--warnings",
				"--notices",
				"--module", getModule().get(),
				"--output-partial-info-plist", getTemporaryDir().getAbsolutePath() + "/" + FilenameUtils.removeExtension(source.getName()) + "-SBPartialInfo.plist",
				"--auto-activate-custom-fonts",
				"--target-device", "iphone", "--target-device", "ipad",
				"--minimum-deployment-target", "13.2",
				"--output-format", "human-readable-text",
				"--compilation-directory", getDestinationDirectory().get().getAsFile().getAbsolutePath() + "/" + source.getParentFile().getName(),
				source.getAbsolutePath())
			.newInvocation()
			.appendStandardStreamToFile(new File(getTemporaryDir(), "outputs.txt"))
			.buildAndSubmit(objects.newInstance(GradleWorkerExecutorEngine.class));
	}
}
