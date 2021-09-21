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

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class CreateIosApplicationBundleTask extends DefaultTask {
	private final Property<FileSystemLocation> applicationBundle;
	private final RegularFileProperty executable;
	private final Property<Boolean> swiftSupportRequired;
	private final ConfigurableFileCollection sources;
	private final ConfigurableFileCollection plugIns;
	private final ConfigurableFileCollection frameworks;
	private final FileSystemOperations fileOperations;
	private final ExecOperations execOperations;

	@OutputDirectory
	public Property<FileSystemLocation> getApplicationBundle() {
		return applicationBundle;
	}

	@Internal
	public RegularFileProperty getExecutable() {
		return executable;
	}

	@Input
	public Property<Boolean> getSwiftSupportRequired() {
		return swiftSupportRequired;
	}

	@Internal
	public ConfigurableFileCollection getSources() {
		return sources;
	}

	@SkipWhenEmpty
	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	protected FileTree getInputFiles() {
		return getSources().getAsFileTree();
	}

	@InputFiles
	public ConfigurableFileCollection getPlugIns() {
		return plugIns;
	}

	@InputFiles
	public ConfigurableFileCollection getFrameworks() {
		return frameworks;
	}

	@Inject
	public CreateIosApplicationBundleTask(ObjectFactory objects, FileSystemOperations fileOperations, ExecOperations execOperations) {
		this.applicationBundle = objects.property(FileSystemLocation.class);
		this.executable = objects.fileProperty();
		this.swiftSupportRequired = objects.property(Boolean.class);
		this.sources = objects.fileCollection();
		this.plugIns = objects.fileCollection();
		this.frameworks = objects.fileCollection();
		this.fileOperations = fileOperations;
		this.execOperations = execOperations;
	}

	@TaskAction
	private void create() throws IOException {
		fileOperations.sync(spec -> {
			spec.from(getSources().getFiles());

			for (File file : getFrameworks().getFiles()) {
				if (file.isDirectory()) {
					spec.from(file, it -> it.into("Frameworks/" + file.getName()));
				} else {
					spec.from(file, it -> it.into("Frameworks"));
				}
			}

			for (File file : getPlugIns().getFiles()) {
				if (file.isDirectory()) {
					spec.from(file, it -> it.into("PlugIns/" + file.getName()));
				} else {
					spec.from(file, it -> it.into("PlugIns"));
				}
			}
			spec.into(getApplicationBundle());
		});

		// Oversimplification of how this file is created
		FileUtils.write(new File(getApplicationBundle().get().getAsFile(), "PkgInfo"), "APPL????", Charset.defaultCharset());

		// TODO: This could probably be a strategy/policy added to the task.
		//  It could also be an doLast action added to the task.
		if (getSwiftSupportRequired().get()) {
			execOperations.exec(spec -> {
				File bundleFile = getExecutable().get().getAsFile();
				File bundleDir = getApplicationBundle().get().getAsFile();
				spec.executable(getSwiftStdlibTool());
				spec.args(
					"--copy",
					"--scan-executable", bundleFile.getAbsolutePath(),
					"--destination", new File(bundleDir, "Frameworks").getAbsolutePath(),
					"--platform", "iphonesimulator",
					"--scan-folder", new File(bundleDir, "Frameworks").getAbsolutePath()
				);
			});
		}
	}

	private static String getSwiftStdlibTool() {
		return CommandLine.of("xcrun", "--sdk", "iphonesimulator", "--find", "swift-stdlib-tool")
			.execute(new ProcessBuilderEngine())
			.waitFor()
			.assertNormalExitValue()
			.getStandardOutput()
			.getAsString()
			.trim();
	}
}
