/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.utils.FileSystemLocationUtils;
import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCProjectReference;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.ConsoleRenderer;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static dev.nokee.buildadapter.xcode.internal.plugins.XCBuildSettingsUtils.codeSigningDisabled;
import static dev.nokee.utils.ProviderUtils.ifPresent;

public abstract class XcodeTargetExecTask extends DefaultTask implements XcodebuildExecTask {
	@Inject
	protected abstract ExecOperations getExecOperations();

	@Internal
	public abstract Property<XCProjectReference> getXcodeProject();

	@Input
	public abstract Property<String> getTargetName();

	@InputFiles
	public abstract ConfigurableFileCollection getInputFiles();

	@InputFiles
	public abstract ConfigurableFileCollection getInputDerivedData();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDirectory();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@TaskAction
	private void doExec() throws IOException {
		getFileOperations().sync(spec -> {
			spec.from(getInputDerivedData());
			spec.into(getDerivedDataPath());
		});

		ExecResult result = null;
		try (val outStream = new FileOutputStream(new File(getTemporaryDir(), "outputs.txt"))) {
			result = getExecOperations().exec(spec -> {
				spec.commandLine("xcodebuild", "-project", getXcodeProject().get().getLocation(), "-target", getTargetName().get());
				spec.args(getDerivedDataPath().map(FileSystemLocationUtils::asPath).map(derivedDataPath -> {
					return ImmutableList.of("PODS_BUILD_DIR=" + derivedDataPath.resolve("Build/Products"));
				}).get());
				spec.args(getDerivedDataPath().map(FileSystemLocationUtils::asPath)
					.map(new DerivedDataPathAsBuildSettings()).map(this::asFlags).get());
				ifPresent(getSdk(), sdk -> spec.args("-sdk", sdk));
				ifPresent(getConfiguration(), buildType -> spec.args("-configuration", buildType));
				spec.args(codeSigningDisabled());
				ifPresent(getWorkingDirectory(), spec::workingDir);
				spec.setStandardOutput(outStream);
				spec.setErrorOutput(outStream);
				spec.setIgnoreExitValue(true);
			});
		}

		if (result.getExitValue() != 0) {
			throw new RuntimeException(String.format("Process '%s' finished with non-zero exit value %d, see %s for more information.", "xcodebuild", result.getExitValue(), new ConsoleRenderer().asClickableFileUrl(new File(getTemporaryDir(), "outputs.txt"))));
		}

		getFileOperations().sync(spec -> {
			spec.from(getDerivedDataPath(), it -> it.include("Build/Products/**/*"));
			spec.into(getOutputDirectory());
		});
	}

	private List<String> asFlags(XCBuildSettings buildSettings) {
		val builder = ImmutableList.<String>builder();
		buildSettings.forEach((k, v) -> builder.add(k + "=" + v));
		return builder.build();
	}
}
