package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
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
public abstract class SignedIosApplicationBundleInternal implements Binary, Buildable {
	private final TaskProvider<SignIosApplicationBundleTask> bundleTask;

	@Inject
	public SignedIosApplicationBundleInternal(TaskProvider<SignIosApplicationBundleTask> bundleTask) {
		this.bundleTask = bundleTask;
	}

	@Inject
	protected abstract TaskContainer getTasks();

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
