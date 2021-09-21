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
package dev.nokee.testing.xctest.tasks.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

public class CreateIosXCTestBundleTask extends DefaultTask {
	private final Property<FileSystemLocation> xCTestBundle;
	private final ConfigurableFileCollection sources;
	private final FileSystemOperations fileOperations;

	@OutputDirectory
	public Property<FileSystemLocation> getXCTestBundle() {
		return xCTestBundle;
	}

	@InputFiles
	public ConfigurableFileCollection getSources() {
		return sources;
	}

	@Inject
	public CreateIosXCTestBundleTask(ObjectFactory objects, FileSystemOperations fileOperations) {
		this.xCTestBundle = objects.property(FileSystemLocation.class);
		this.sources = objects.fileCollection();
		this.fileOperations = fileOperations;
	}

	@TaskAction
	private void create() {
		fileOperations.sync(spec -> {
			spec.from(getSources().getFiles());
			spec.into(getXCTestBundle());
		});
	}
}
