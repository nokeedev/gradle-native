package dev.nokee.testing.xctest.internal;

import dev.nokee.platform.base.Binary;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public final class IosXCTestBundle implements Binary {
	private final TaskProvider<CreateIosXCTestBundleTask> createTask;

	IosXCTestBundle(TaskProvider<CreateIosXCTestBundleTask> createTask) {
		this.createTask = createTask;
	}

	public Provider<FileSystemLocation> getXCTestBundleLocation() {
		return createTask.flatMap(CreateIosXCTestBundleTask::getXCTestBundle);
	}
}
