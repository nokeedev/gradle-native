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

import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.ConfigurableSetContainer;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.TargetDependenciesAwareBuilder;
import dev.nokee.xcode.project.PBXObjectArchiver;
import dev.nokee.xcode.project.PBXProjWriter;
import lombok.val;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.of;

public abstract class XCTargetIsolationTask extends ParameterizedTask<XCTargetIsolationTask.Parameters> {

	@Inject
	public XCTargetIsolationTask(WorkerExecutor workerExecutor) {
		super(TaskAction.class, workerExecutor::noIsolation);
	}

	public interface Parameters extends WorkParameters, CopyTo<Parameters> {
		@Nested
		ConfigurableIsolations getIsolations();

		@Nested
		ConfigurableXCProjectLocation getOriginalProject();

		@OutputDirectory
		DirectoryProperty getIsolatedProjectLocation();

		@Override
		default CopyTo<Parameters> copyTo(Parameters other) {
			other.getIsolatedProjectLocation().set(getIsolatedProjectLocation());
			other.getOriginalProject().getLocation().set(getOriginalProject().getLocation());
			other.getIsolations().addAll(getIsolations());
			return this;
		}
	}

	@Nested
	protected Object getOriginalProjectInputFiles() {
		return getParameters().getOriginalProject().asInput();
	}

	public static abstract class ConfigurableIsolations extends ConfigurableSetContainer<IsolationSpec> {}

	public interface IsolationSpec {
		PBXProject apply(PBXProject project);
	}

	public static abstract class IsolateTargetSpec implements IsolationSpec {
		@Input
		public abstract Property<String> getTargetNameToIsolate();

		@Override
		public PBXProject apply(PBXProject project) {
			return project.toBuilder()
				.targets(project.getTargets().stream()
					.filter(target -> target.getName().equals(getTargetNameToIsolate().get()))
					.map(target -> {
						val builder = target.toBuilder();
						((TargetDependenciesAwareBuilder<?>) builder).dependencies(of());
						return builder.build();
					}).collect(Collectors.toList()))
				.build();
		}
	}

	public static abstract class AddPackageProductDependenciesSpec implements IsolationSpec {
		@Input
		public abstract SetProperty<XCSwiftPackageProductDependency> getPackageProductDependencies();

		@Input
		public abstract Property<String> getTargetNameToIsolate();

		@Override
		public PBXProject apply(PBXProject project) {
			val projectBuilder = project.toBuilder()
				.targets(project.getTargets().stream()
					.map(target -> {
						if (target.getName().equals(getTargetNameToIsolate().get()) && target instanceof PBXNativeTarget) {
							val targetBuilder = target.toBuilder();

							getPackageProductDependencies().get().forEach(((PBXNativeTarget.Builder) targetBuilder)::packageProductDependency);

							return targetBuilder.build();
						} else {
							return target;
						}
					}).collect(Collectors.toList()));

			getPackageProductDependencies().get().forEach(it -> projectBuilder.packageReference(it.getPackageReference()));

			return projectBuilder.build();
		}
	}

	public static abstract class TaskAction implements WorkAction<Parameters> {
		private final FileSystemOperations fileOperations;

		@Inject
		public TaskAction(FileSystemOperations fileOperations) {
			this.fileOperations = fileOperations;
		}

		@Override
		public void execute() {
			val originalProjectReference = getParameters().getOriginalProject().getAsReference().get();
			val isolatedProjectLocation = getParameters().getIsolatedProjectLocation().get().getAsFile().toPath();
			fileOperations.sync(spec -> {
				spec.from(originalProjectReference.getLocation());
				spec.into(isolatedProjectLocation);
			});

			PBXProject project = XCLoaders.pbxprojectLoader().load(originalProjectReference);

			project = project.toBuilder().projectDirPath(originalProjectReference.getLocation().getParent().toString()).build();

			for (IsolationSpec spec : getParameters().getIsolations().getElements().get()) {
				project = spec.apply(project);
			}

			try (val writer = new PBXProjWriter(Files.newBufferedWriter(isolatedProjectLocation.resolve("project.pbxproj")))) {
				writer.write(new PBXObjectArchiver().encode(project));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
