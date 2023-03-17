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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.buildadapter.xcode.internal.files.PreserveLastModifiedFileSystemOperation;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
public abstract class AssembleDerivedDataDirectoryTask extends DefaultTask {
	private final WorkerExecutor workerExecutor;

	@InputFiles
	public abstract ConfigurableFileCollection getIncomingDerivedDataPaths();

	@OutputFiles
	public abstract ConfigurableFileCollection getOutputFiles();

	@Internal
	public abstract DirectoryProperty getXcodeDerivedDataPath();

	@Inject
	public AssembleDerivedDataDirectoryTask(WorkerExecutor workerExecutor) {
		this.workerExecutor = workerExecutor;
		getOutputFiles().from(getXcodeDerivedDataPath().dir("Build/Products").map(Directory::getAsFileTree)); // only track Build/Products/**
	}

	@TaskAction
	private void doAssemble() {
		workerExecutor.noIsolation().submit(CopyAction.class, parameters -> {
			parameters.getIncomingDerivedDataPaths().from(getIncomingDerivedDataPaths());
			parameters.getXcodeDerivedDataPath().set(getXcodeDerivedDataPath());
		});
	}

	public static abstract class CopyAction implements WorkAction<CopyAction.Parameters> {
		private final FileSystemOperations fileOperations;

		public interface Parameters extends WorkParameters, DerivedDataAssemblingRunnable.Parameters {
			ConfigurableFileCollection getIncomingDerivedDataPaths();
			DirectoryProperty getXcodeDerivedDataPath();
		}

		@Inject
		public CopyAction(FileSystemOperations fileOperations) {
			this.fileOperations = fileOperations;
		}

		@Override
		public void execute() {
			new DerivedDataAssemblingRunnable(fileOperations, getParameters()).run();
		}
	}

	public static final class DerivedDataAssemblingRunnable implements Runnable {
		private final FileSystemOperations fileOperations;
		private final Parameters parameters;

		public DerivedDataAssemblingRunnable(FileSystemOperations fileOperations, Parameters parameters) {
			this.fileOperations = fileOperations;
			this.parameters = parameters;
		}

		@Override
		public void run() {
			new PreserveLastModifiedFileSystemOperation(fileOperations::copy).execute(spec -> {
				spec.from(parameters.getIncomingDerivedDataPaths());
				spec.into(parameters.getXcodeDerivedDataPath());
				spec.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
			});
		}

		public interface Parameters {
			ConfigurableFileCollection getIncomingDerivedDataPaths();
			DirectoryProperty getXcodeDerivedDataPath();
		}
	}
}
