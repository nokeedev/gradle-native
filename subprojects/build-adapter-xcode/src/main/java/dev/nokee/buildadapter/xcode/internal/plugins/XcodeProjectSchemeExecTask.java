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

import dev.nokee.utils.FileSystemLocationUtils;
import dev.nokee.xcode.XCProjectReference;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static dev.nokee.utils.ProviderUtils.ifPresent;

public abstract class XcodeProjectSchemeExecTask extends DefaultTask implements XcodebuildExecTask {
	@Inject
	protected abstract ExecOperations getExecOperations();

	@Internal
	public abstract Property<XCProjectReference> getXcodeProject();

	@Internal
	public abstract Property<String> getSchemeName();

	// SHOW BUILD SETTINGS:
	// outputs BUILT_PRODUCT_DIR -> shows where the thing is... maybe...

	// process
	// COPY dependencies' isolated derived data to project's derived data (same files should be skipped to avoid timestamp change or keep timestamp)
	// BUILD TO using project derived data
	// Sync scheme output to isolated directory --> cache this directory
	// EXPOSE isolated dir via dependency engine

	@Internal
	public Provider<String> getProjectName() {
		return getXcodeProject().map(it -> FilenameUtils.removeExtension(it.getLocation().getFileName().toString()));
	}

	@TaskAction
	private void doExec() throws IOException {
		try (val outStream = new FileOutputStream(new File(getTemporaryDir(), "outputs.txt"))) {
			getExecOperations().exec(spec -> {
				spec.commandLine("xcodebuild", "-project", getXcodeProject().get().getLocation(), "-scheme", getSchemeName().get());
				ifPresent(getDerivedDataPath().map(FileSystemLocationUtils::asPath), derivedDataPath -> {
					spec.args("-derivedDataPath", derivedDataPath);
					spec.args("PODS_BUILD_DIR=" + derivedDataPath.resolve("Build/Products"));
					spec.args("BUILD_DIR=" + derivedDataPath.resolve("Build/Products"));
					spec.args("BUILD_ROOT=" + derivedDataPath.resolve("Build/Products"));
					spec.args("PROJECT_TEMP_DIR=" + derivedDataPath.resolve("Build/Intermediates.noindex/" + getProjectName().get() + ".build"));
					spec.args("OBJROOT=" + derivedDataPath.resolve("Build/Intermediates.noindex"));
					spec.args("SYMROOT=" + derivedDataPath.resolve("Build/Products"));
				});
				ifPresent(getSdk(), sdk -> spec.args("-sdk", sdk));
				ifPresent(getConfiguration(), buildType -> spec.args("-configuration", buildType));
				spec.args(
					// Disable code signing, see https://stackoverflow.com/a/39901677/13624023
					"CODE_SIGN_IDENTITY=\"\"", "CODE_SIGNING_REQUIRED=NO", "CODE_SIGN_ENTITLEMENTS=\"\"", "CODE_SIGNING_ALLOWED=\"NO\"");
				ifPresent(getWorkingDirectory(), spec::workingDir);
				spec.setStandardOutput(outStream);
				spec.setErrorOutput(outStream);
			});
		}
	}
}
