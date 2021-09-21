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
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;

public class AssetCatalogCompileTask extends DefaultTask {
	private final DirectoryProperty destinationDirectory;
	private final RegularFileProperty source;
	private final Property<String> identifier;
	private final Property<CommandLineTool> assetCompilerTool;
	private final ObjectFactory objects;

	@OutputDirectory
	public DirectoryProperty getDestinationDirectory() {
		return destinationDirectory;
	}

	@SkipWhenEmpty
	@InputDirectory
	public RegularFileProperty getSource() {
		return source;
	}

	@Input
	public Property<String> getIdentifier() {
		return identifier;
	}

	@Nested
	public Property<CommandLineTool> getAssetCompilerTool() {
		return assetCompilerTool;
	}

	@Inject
	public AssetCatalogCompileTask(ObjectFactory objects) {
		this.destinationDirectory = objects.directoryProperty();
		this.source = objects.fileProperty();
		this.identifier = objects.property(String.class);
		this.assetCompilerTool = objects.property(CommandLineTool.class);
		this.objects = objects;
	}

	@TaskAction
	private void compile() {
		getAssetCompilerTool().get()
			.withArguments(
				"--output-format", "human-readable-text",
				"--notices", "--warnings",
				"--export-dependency-info", getTemporaryDir().getAbsolutePath() + "/assetcatalog_dependencies",
				"--output-partial-info-plist", getTemporaryDir().getAbsolutePath() + "/assetcatalog_generated_info.plist",
				"--app-icon", "AppIcon",
				"--compress-pngs",
				"--enable-on-demand-resources", "YES",
				"--filter-for-device-model", "iPhone10,5",
				"--filter-for-device-os-version", "13.2",
				"--sticker-pack-identifier-prefix", getIdentifier().get() + ".sticker-pack.",
				"--target-device", "iphone", "--target-device", "ipad",
				"--minimum-deployment-target", "11.2",
				"--platform", "iphonesimulator",
				"--product-type", "com.apple.product-type.application",
				"--compile", getDestinationDirectory().get().getAsFile().getAbsolutePath(), getSource().get().getAsFile().getAbsolutePath())
			.newInvocation()
			.appendStandardStreamToFile(new File(getTemporaryDir(), "outputs.txt"))
			.buildAndSubmit(objects.newInstance(GradleWorkerExecutorEngine.class));
	}
}
