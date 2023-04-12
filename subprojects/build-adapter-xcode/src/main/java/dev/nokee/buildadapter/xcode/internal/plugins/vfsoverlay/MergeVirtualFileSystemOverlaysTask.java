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

import dev.nokee.buildadapter.xcode.internal.plugins.CopyTo;
import dev.nokee.buildadapter.xcode.internal.plugins.ParameterizedTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class MergeVirtualFileSystemOverlaysTask extends ParameterizedTask<MergeVirtualFileSystemOverlaysTask.Parameters> {
	@Inject
	public MergeVirtualFileSystemOverlaysTask(WorkerExecutor workExecutor) {
		super(TaskAction.class, workExecutor::noIsolation);
	}

	public interface Parameters extends WorkParameters, CopyTo<Parameters> {
		@InputFiles
		ConfigurableFileCollection getSources();

		@Internal
		DirectoryProperty getDerivedDataPath();

		@OutputFile
		RegularFileProperty getOutputFile();

		@Override
		default CopyTo<Parameters> copyTo(Parameters other) {
			other.getSources().setFrom(getSources());
			other.getDerivedDataPath().set(getDerivedDataPath());
			other.getOutputFile().set(getOutputFile());
			return this;
		}
	}

	@Input
	protected final Provider<String> getDerivedDataPathLocation() {
		return getParameters().getDerivedDataPath().map(it -> it.getAsFile().getAbsolutePath());
	}

	public static abstract class TaskAction implements WorkAction<Parameters> {
		@Override
		public void execute() {
			try {
				final VirtualFileSystemOverlayMerger merger = new VirtualFileSystemOverlayMerger();
				getParameters().getSources().getFiles().forEach(it -> merger.withOverlayFile(it.toPath()));
				merger.rebaseOn(getParameters().getDerivedDataPath().get().getAsFile().toPath());
				merger.mergeTo(getParameters().getOutputFile().get().getAsFile().toPath());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
