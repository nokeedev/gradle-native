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

import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.project.CodeableXCSwiftPackageProductDependency;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.tools.ant.taskdefs.XSLTProcess;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public abstract class GenerateSwiftPackageManifestTask extends ParameterizedTask<GenerateSwiftPackageManifestTask.Parameters> {
	@Inject
	public GenerateSwiftPackageManifestTask(WorkerExecutor workerExecutor) {
		super(TaskAction.class, workerExecutor::noIsolation);
	}

	public interface Parameters extends WorkParameters, CopyTo<Parameters> {
		@InputDirectory
		DirectoryProperty getProjectLocation();

		@Input
		Property<String> getTargetName();

		@OutputFile
		RegularFileProperty getManifestFile();

		@Override
		default CopyTo<Parameters> copyTo(Parameters other) {
			other.getManifestFile().set(getManifestFile());
			other.getTargetName().set(getTargetName());
			other.getProjectLocation().set(getProjectLocation());
			return this;
		}
	}

	public static abstract class TaskAction implements WorkAction<Parameters> {
		@Override
		public void execute() {
			val target = XCLoaders.pbxtargetLoader().load(XCProjectReference.of(getParameters().getProjectLocation().get().getAsFile().toPath()).ofTarget(getParameters().getTargetName().get()));
			ArrayList<XCTargetIsolationTask.PackageRef> packagesGids = new ArrayList<>();
			if (target instanceof PBXNativeTarget) {
				((PBXNativeTarget) target).getPackageProductDependencies().stream().forEach(it -> packagesGids.add(new XCTargetIsolationTask.PackageRef(it.getProductName(), ((CodeableXCSwiftPackageProductDependency) it).globalId())));
			}

			try (val outStream = Files.newOutputStream(getParameters().getManifestFile().get().getAsFile().toPath())) {
				SerializationUtils.serialize(packagesGids, outStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
