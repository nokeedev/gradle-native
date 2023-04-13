/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay;

import com.google.common.collect.Streams;
import dev.nokee.buildadapter.xcode.internal.plugins.CopyTo;
import dev.nokee.buildadapter.xcode.internal.plugins.ParameterizedTask;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay.VirtualDirectory.file;

public abstract class GenerateVirtualFileSystemOverlaysTask extends ParameterizedTask<GenerateVirtualFileSystemOverlaysTask.Parameters> {
	@Inject
	public GenerateVirtualFileSystemOverlaysTask(WorkerExecutor workerExecutor) {
		super(TaskAction.class, workerExecutor::noIsolation);

//		// TODO: Configure from the outside
//		val xcodebuildLayer = new XcodebuildBuildSettingLayer.Builder(objects)
//			.targetReference(getParameters().getTargetReference())
//			.sdk(getParameters().getSdk())
//			.configuration(getParameters().getConfiguration())
//			.developerDir(getParameters().getXcodeInstallation().map(XcodeInstallation::getDeveloperDirectory))
//			.buildSettings(getParameters().getBuildSettings().asProvider().map(buildSettingsOverride()))
//			.build();
//		getParameters().getBuildSettings().setFrom(xcodebuildLayer);
//
//		// TODO: Configure from the outside
//		val derivedDataOverride = objects.mapProperty(String.class, String.class);
//		derivedDataOverride.put("OBJROOT", getParameters().getDerivedDataPath().map(it -> it.dir("Build/Intermediates.noindex").getAsFile().getAbsolutePath()));
//		derivedDataOverride.put("SYMROOT", getParameters().getDerivedDataPath().map(it -> it.dir("Build/Products").getAsFile().getAbsolutePath()));
//		getParameters().getBuildSettings().from(derivedDataOverride);
	}

	public interface Parameters extends WorkParameters, CopyTo<Parameters> {
		@Nested
		ConfigurableOverlays getOverlays();

		default void overlays(Action<? super ConfigurableOverlays> action) {
			action.execute(getOverlays());
		}

		@OutputFile
		RegularFileProperty getOutputFile();

		@Override
		default Parameters copyTo(Parameters other) {
			other.getOutputFile().set(getOutputFile());
			other.getOverlays().addAll(getOverlays());
			return this;
		}
	}

	public static abstract class TaskAction implements org.gradle.workers.WorkAction<Parameters> {
		private final FileSystem fileSystem;

		@Inject
		public TaskAction() {
			this(FileSystems.getDefault());
		}

		protected TaskAction(FileSystem fileSystem) {
			this.fileSystem = fileSystem;
		}

		private Path outputFile() {
			return getParameters().getOutputFile().get().getAsFile().toPath();
		}

		@Override
		public void execute() {
			val virtualDirectories = new ArrayList<VirtualFileSystemOverlay.VirtualDirectory>();
			for (VFSOverlaySpec overlay : getParameters().getOverlays().getElements().get()) {
				virtualDirectories.add(new VirtualFileSystemOverlay.VirtualDirectory(overlay.getName().get(), Streams.stream(overlay.getEntries().getElements().get()).map(it -> file(it.getName().get(), fileSystem.getPath(it.getLocation().get()))).collect(Collectors.toList())));
			}

			try (val writer = new VirtualFileSystemOverlayWriter(Files.newBufferedWriter(outputFile()))) {
				writer.write(new VirtualFileSystemOverlay(virtualDirectories));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
