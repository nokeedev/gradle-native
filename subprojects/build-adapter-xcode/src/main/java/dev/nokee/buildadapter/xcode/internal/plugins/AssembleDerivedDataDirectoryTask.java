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

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class AssembleDerivedDataDirectoryTask extends DefaultTask {
	private final WorkerExecutor workerExecutor;
	private final ObjectFactory objects;

	@InputFiles
	@Incremental
	public abstract ConfigurableFileCollection getInputFiles();

	@OutputFiles
	public abstract ConfigurableFileCollection getOutputFiles();
//	public abstract ConfigurableFileTree getOutputFiles();

	@Internal
	public abstract DirectoryProperty getDerivedDataDirectory();

	@Inject
	public AssembleDerivedDataDirectoryTask(WorkerExecutor workerExecutor, ObjectFactory objects) {
		this.workerExecutor = workerExecutor;
		this.objects = objects;
//		getOutputFiles().setDir(getDerivedDataDirectory());
//		getOutputFiles().include(new Spec<FileTreeElement>() {
//			private Set<String> included;
//
//			@Override
//			public boolean isSatisfiedBy(FileTreeElement element) {
//				return included().contains(element.getPath());
//			}
//
//			private Set<String> included() {
//				if (included == null) {
//					included = new HashSet<>();
//					getInputFiles().getAsFileTree().visit(details -> {
//						included.add(details.getPath());
//					});
//				}
//				return included;
//			}
//		});
		getOutputFiles().from(getDerivedDataDirectory().map(derivedDataDir -> {
			val result = new ArrayList<>();
			getInputFiles().getAsFileTree().visit(details -> {
				if (!details.isDirectory()) {
					result.add(derivedDataDir.file(details.getPath()));
				}
			});
//			System.out.println("ALL OUTPUT FILES of " + AssembleDerivedDataDirectoryTask.this + " => " + result);
			return result;
		}));
		getOutputFiles().finalizeValueOnRead();
	}

	@TaskAction
	private void doAssemble(InputChanges changes) throws IOException {
		workerExecutor.noIsolation().submit(CopyWorkAction.class, parameters -> {
			val derivedDataDir = getDerivedDataDirectory().get().getAsFile().toPath();

			val map = new HashMap<File, String>();
			getInputFiles().getAsFileTree().visit(details -> {
				map.put(details.getFile(), details.getPath());
			});

			for (FileChange fileChange : changes.getFileChanges(getInputFiles())) {
				val target = derivedDataDir.resolve(map.getOrDefault(fileChange.getFile(), ""));
				switch (fileChange.getChangeType()) {
					case ADDED:
					case MODIFIED:
						if (!fileChange.getFile().isDirectory() && fileChange.getFile().exists()) {
							val change = objects.newInstance(CopyFileChange.class);
							change.getSource().set(fileChange.getFile());
							change.getDestination().set(target.toFile());
							parameters.getFileChanges().add(change);
//							Files.createDirectories(target.getParent());
//							Files.copy(fileChange.getFile().toPath(), target, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
//							assert Files.exists(target);
						}
						break;
					case REMOVED:
						if (Files.isDirectory(target)) {
							val change = objects.newInstance(DeleteDirectoryChange.class);
							change.getDirectory().set(target.toFile());
							parameters.getFileChanges().add(change);
//							FileUtils.deleteDirectory(target.toFile());
						} else {
							val change = objects.newInstance(DeleteFileChange.class);
							change.getFile().set(target.toFile());
							parameters.getFileChanges().add(change);
//							Files.deleteIfExists(target);
						}
						break;
				}
			}

			for (FileChange fileChange : changes.getFileChanges(getInputFiles())) {
				if (fileChange.getFile().isDirectory() && !fileChange.getChangeType().equals(ChangeType.REMOVED)) {
					System.out.println(fileChange);
					val change = objects.newInstance(RestoreLastModifiedChange.class);
					change.getSource().set(fileChange.getFile());
					change.getDestination().set(derivedDataDir.resolve(map.getOrDefault(fileChange.getFile(), "")).toFile());
					parameters.getFileChanges().add(change);
//					val target = derivedDataDir.resolve(map.getOrDefault(fileChange.getFile(), ""));
//					Files.setLastModifiedTime(target.getParent(), Files.getLastModifiedTime(fileChange.getFile().toPath()));
				}
			}
		});
	}

	public static abstract class CopyWorkAction implements WorkAction<CopyWorkAction.Parameters> {
		interface Parameters extends WorkParameters {
			ListProperty<MyFileChange> getFileChanges();
		}

		@Override
		public void execute() {
			try {
				for (MyFileChange myFileChange : getParameters().getFileChanges().get()) {
					myFileChange.execute();
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	public interface MyFileChange {
		void execute() throws IOException;
	}

	public interface CopyFileChange extends MyFileChange {
		RegularFileProperty getSource();
		RegularFileProperty getDestination();

		@Override
		default void execute() throws IOException {
			val source = getSource().get().getAsFile().toPath();
			val target = getDestination().get().getAsFile().toPath();

			System.out.println("COPY FILE " + source + " to " + target);

			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public interface DeleteFileChange extends MyFileChange {
		RegularFileProperty getFile();

		@Override
		default void execute() throws IOException {
			val target = getFile().getAsFile().get().toPath();
			Files.deleteIfExists(target);
		}
	}

	public interface DeleteDirectoryChange extends MyFileChange {
		RegularFileProperty getDirectory();

		@Override
		default void execute() throws IOException {
			val target = getDirectory().getAsFile().get().toPath();
			FileUtils.deleteDirectory(target.toFile());
		}
	}

	public interface RestoreLastModifiedChange extends MyFileChange {
		DirectoryProperty getSource();
		DirectoryProperty getDestination();

		@Override
		default void execute() throws IOException {
			val source = getSource().get().getAsFile().toPath();
			val target = getDestination().get().getAsFile().toPath();

			System.out.println("RESTORING LAST MODE on " + target + " from " + source);

			if (Files.exists(target)) {
				Files.setLastModifiedTime(target, Files.getLastModifiedTime(source));
			}
		}
	}
}
