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

import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.project.PBXObjectReference;
import dev.nokee.xcode.project.PBXProj;
import dev.nokee.xcode.project.PBXProjReader;
import dev.nokee.xcode.project.PBXProjWriter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public abstract class IsolateTargetTask extends DefaultTask {
	private final WorkerExecutor workerExecutor;
	private final ObjectFactory objects;

	@Internal
	public abstract Property<XCProjectReference> getProjectReference();

	@InputFiles
	@Incremental
	public abstract ConfigurableFileCollection getInputFiles();

//	@OutputFiles
//	public abstract ConfigurableFileCollection getOutputFiles();
//	public abstract ConfigurableFileTree getOutputFiles();

	@OutputDirectory
	public abstract DirectoryProperty getIsolatedProjectLocation();

	@Internal
	public abstract Property<XCProjectReference> getIsolatedProjectReference();

	@Inject
	public IsolateTargetTask(WorkerExecutor workerExecutor, ObjectFactory objects, ProviderFactory providers) {
		this.workerExecutor = workerExecutor;
		this.objects = objects;
		getInputFiles().from(objects.fileTree().setDir(getProjectReference().map(XCProjectReference::getLocation)).exclude("xcuserdata"));
		getIsolatedProjectReference().set(getIsolatedProjectLocation().flatMap(it -> providers.provider(() -> XCProjectReference.of(it.getAsFile().toPath()))));
	}

	@TaskAction
	private void doAssemble(InputChanges changes) throws IOException {
		workerExecutor.noIsolation().submit(CopyWorkAction.class, parameters -> {
			val derivedDataDir = getIsolatedProjectLocation().get().getAsFile().toPath();

			val map = new HashMap<File, String>();
			getInputFiles().getAsFileTree().visit(details -> {
				map.put(details.getFile(), details.getPath());
			});

			for (FileChange fileChange : changes.getFileChanges(getInputFiles())) {
//				System.out.println(fileChange);
				val target = derivedDataDir.resolve(map.getOrDefault(fileChange.getFile(), ""));
				switch (fileChange.getChangeType()) {
					case ADDED:
					case MODIFIED:
						if (!fileChange.getFile().isDirectory()) {
							if (fileChange.getFile().getName().equals("project.pbxproj")) {
								val change = objects.newInstance(IsolateChange.class);
								change.getSource().set(fileChange.getFile());
								change.getDestination().set(target.toFile());
								change.getProjectPathDir().set(getProjectReference().get().getLocation().getParent().toFile());
								parameters.getFileChanges().add(change);
							} else {
								val change = objects.newInstance(CopyFileChange.class);
								change.getSource().set(fileChange.getFile());
								change.getDestination().set(target.toFile());
								parameters.getFileChanges().add(change);
							}
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
				if (fileChange.getFile().isDirectory()) {
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
			val target = getDestination().get().getAsFile().toPath();
			val source = getSource().get().getAsFile().toPath();

//			System.out.println("COPY " + source + " to " + target);

			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public interface DeleteFileChange extends MyFileChange {
		RegularFileProperty getFile();

		@Override
		default void execute() throws IOException {
			val target = getFile().getAsFile().get().toPath();

//			System.out.println("DELETE file " + target);

			Files.deleteIfExists(target);
		}
	}

	public interface DeleteDirectoryChange extends MyFileChange {
		RegularFileProperty getDirectory();

		@Override
		default void execute() throws IOException {
			val target = getDirectory().getAsFile().get().toPath();

//			System.out.println("DELETE dir " + target);

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

//			System.out.println("Revert " + target);

			if (Files.exists(target) && Files.exists(source)) {
				Files.setLastModifiedTime(target, Files.getLastModifiedTime(source));
			}
		}
	}

	public interface IsolateChange extends MyFileChange {
		RegularFileProperty getSource();
		RegularFileProperty getDestination();
		DirectoryProperty getProjectPathDir();

		@Override
		default void execute() throws IOException {
			val target = getDestination().get().getAsFile().toPath();
			val source = getSource().get().getAsFile().toPath();

//			System.out.println("ISOLATE " + source);

			assert target.endsWith("project.pbxproj");
			assert source.endsWith("project.pbxproj");

			val lastModTime = Files.getLastModifiedTime(source);
			PBXProj proj;
			try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(source)))) {
				proj = reader.read();
			}
			val builder = PBXProj.builder();
			val isolatedProject = builder.rootObject(proj.getRootObject()).objects(o -> {
				for (PBXObjectReference object : proj.getObjects()) {
					if (ImmutableSet.of("PBXNativeTarget", "PBXAggregateTarget", "PBXLegacyTarget").contains(object.isa())) {
						o.add(PBXObjectReference.of(object.getGlobalID(), entryBuilder -> {
							for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
								if (!entry.getKey().equals("dependencies")) {
									entryBuilder.putField(entry.getKey(), entry.getValue());
								}
							}
						}));
					} else if ("PBXProject".equals(object.isa())) {
						o.add(PBXObjectReference.of(object.getGlobalID(), entryBuilder -> {
							entryBuilder.putField("projectDirPath", getProjectPathDir().get().getAsFile().getAbsolutePath());
							for (Map.Entry<String, Object> entry : object.getFields().entrySet()) {
								if (!entry.getKey().equals("projectDirPath")) {
									entryBuilder.putField(entry.getKey(), entry.getValue());
								}
							}
						}));
					} else {
						o.add(object);
					}
				}
			}).build();

			Files.createDirectories(target.getParent());
			try (val writer = new PBXProjWriter(Files.newBufferedWriter(target))) {
				writer.write(isolatedProject);
			}
			Files.setLastModifiedTime(target, lastModTime);
		}
	}
}
