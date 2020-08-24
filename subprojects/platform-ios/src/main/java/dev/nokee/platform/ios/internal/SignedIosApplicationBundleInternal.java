package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

// TODO: Not sure about implementing NativeBinary...
//  BaseNativeVariant#getDevelopmentBinary() assume a NativeBinary...
//  There should probably be something high level in Variant or BaseNativeVariant shouldn't be used for iOS variant.
public class SignedIosApplicationBundleInternal implements Binary, Buildable {
	private final TaskProvider<SignIosApplicationBundleTask> bundleTask;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;

	@Inject
	public SignedIosApplicationBundleInternal(TaskProvider<SignIosApplicationBundleTask> bundleTask, TaskContainer tasks) {
		this.bundleTask = bundleTask;
		this.tasks = tasks;
	}

	public TaskProvider<? extends Task> getBundleTask() {
		return bundleTask;
	}

	public boolean isBuildable() {
		// We should check if the tools required are available (codesign, ibtool, actool, etc.)
		return true;
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> ImmutableSet.of(getBundleTask().get());
	}

	public Provider<FileSystemLocation> getApplicationBundleLocation() {
		return bundleTask.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle);
	}
}
