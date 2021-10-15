/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.testing.xctest.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.nativebase.internal.HasOutputFile;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public final class IosXCTestBundle implements Binary, HasOutputFile {
	private final TaskProvider<CreateIosXCTestBundleTask> createTask;

	IosXCTestBundle(TaskProvider<CreateIosXCTestBundleTask> createTask) {
		this.createTask = createTask;
	}

	public Provider<FileSystemLocation> getXCTestBundleLocation() {
		return createTask.flatMap(CreateIosXCTestBundleTask::getXCTestBundle);
	}

	@Override
	public Provider<RegularFile> getOutputFile() {
		return createTask.flatMap(task ->
			task.getProject().getObjects().fileProperty().fileProvider(task.getXCTestBundle().map(it -> it.getAsFile())));
	}
}
