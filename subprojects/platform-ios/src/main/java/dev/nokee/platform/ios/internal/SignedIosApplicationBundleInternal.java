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
package dev.nokee.platform.ios.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

// TODO: Not sure about implementing NativeBinary...
//  BaseNativeVariant#getDevelopmentBinary() assume a NativeBinary...
//  There should probably be something high level in Variant or BaseNativeVariant shouldn't be used for iOS variant.
public /*final*/ class SignedIosApplicationBundleInternal implements SignedIosApplicationBundle, Binary, Buildable {
	private final TaskProvider<SignIosApplicationBundleTask> bundleTask;

	@Inject
	public SignedIosApplicationBundleInternal(TaskProvider<SignIosApplicationBundleTask> bundleTask) {
		this.bundleTask = bundleTask;
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

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}
}
