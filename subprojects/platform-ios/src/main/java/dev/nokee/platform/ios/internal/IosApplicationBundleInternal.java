package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import dev.nokee.platform.nativebase.NativeBinary;
import org.gradle.api.Buildable;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

// TODO: Not sure about implementing NativeBinary...
//  BaseNativeVariant#getDevelopmentBinary() assume a NativeBinary...
//  There should probably be something high level in Variant or BaseNativeVariant shouldn't be used for iOS variant.
public abstract class IosApplicationBundleInternal implements Binary, Buildable {

	@Inject
	protected abstract TaskContainer getTasks();

	public TaskProvider<CreateIosApplicationBundleTask> getBundleTask() {
		return getTasks().named("createApplicationBundle", CreateIosApplicationBundleTask.class);
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
		return getTasks().named("createApplicationBundle", SignIosApplicationBundleTask.class).flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle);
	}
}
