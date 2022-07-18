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
package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.language.nativebase.internal.ObjectSourceSet;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.linking.HasLinkTask;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkTask;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.TaskDependencyUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.toolchain.Swiftc;

import javax.inject.Inject;
import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExecutableBinaryInternal extends BaseNativeBinary implements ExecutableBinary
	, Buildable
	, HasPublicType
	, ModelBackedNamedMixIn
	, ModelBackedHasBaseNameMixIn
	, HasLinkTask<LinkExecutable, LinkExecutableTask>
	, HasObjectFilesToBinaryTask
{
	@Inject
	public ExecutableBinaryInternal(BinaryIdentifier identifier, DomainObjectSet<ObjectSourceSet> objectSourceSets, TargetMachine targetMachine, NativeIncomingDependencies dependencies, ObjectFactory objects, ProjectLayout layout, ProviderFactory providers, TaskView<Task> compileTasks) {
		super(identifier, objectSourceSets, targetMachine, dependencies, objects, layout, providers, compileTasks);

		getCreateOrLinkTask().configure(this::configureExecutableTask);
		getCreateOrLinkTask().configure(task -> {
			task.getLibs().from(dependencies.getLinkLibraries());
			task.getLinkerArgs().addAll(getProviders().provider(() -> dependencies.getLinkFrameworks().getFiles().stream().flatMap(this::toFrameworkFlags).collect(Collectors.toList())));

			task.getLinkerArgs().addAll(task.getToolChain().map(it -> {
				if (it instanceof Swiftc && targetMachine.getOperatingSystemFamily().isMacOs()) {
					// TODO: Support DEVELOPER_DIR or request the xcrun tool from backend
					return ImmutableList.of("-sdk", CommandLine.of("xcrun", "--show-sdk-path").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().getAsString().trim());
				}
				return ImmutableList.of();
			}));
		});
	}

	private Stream<String> toFrameworkFlags(File it) {
		return ImmutableList.of("-F", it.getParent(), "-framework", FilenameUtils.removeExtension(it.getName())).stream();
	}

	private void configureExecutableTask(LinkExecutableTask task) {
		task.setDescription("Links the executable.");
		task.source(getObjectFiles());

		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		// Until we model the build type
		task.getDebuggable().set(false);

		task.getDestinationDirectory().convention(getLayout().getBuildDirectory().dir(identifier.getOutputDirectoryBase("exes")));
		task.getLinkedFile().convention(getExecutableLinkedFile());
	}

	private Provider<RegularFile> getExecutableLinkedFile() {
		return getLayout().getBuildDirectory().file(getBaseName().map(it -> {
			OperatingSystemFamily osFamily = getTargetMachine().getOperatingSystemFamily();
			OperatingSystemOperations osOperations = OperatingSystemOperations.of(osFamily);
			return osOperations.getExecutableName(identifier.getOutputDirectoryBase("exes") + "/" + it);
		}));
	}

	@Override
	public TaskProvider<LinkExecutable> getLinkTask() {
		return (TaskProvider<LinkExecutable>) ModelElements.of(this, NativeLinkTask.class).as(LinkExecutable.class).asProvider();
	}

	@Override
	public TaskProvider<LinkExecutableTask> getCreateOrLinkTask() {
		return (TaskProvider<LinkExecutableTask>) ModelElements.of(this, NativeLinkTask.class).as(LinkExecutableTask.class).asProvider();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.of(getCreateOrLinkTask());
	}

	@Override
	public boolean isBuildable() {
		try {
			return super.isBuildable() && isBuildable(getCreateOrLinkTask().get());
		} catch (Throwable ex) { // because toolchain selection calls xcrun for macOS which doesn't exists on non-mac system
			return false;
		}
	}

	private static boolean isBuildable(LinkExecutable linkTask) {
		AbstractLinkTask linkTaskInternal = (AbstractLinkTask)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(ExecutableBinary.class);
	}

	@Override
	public String toString() {
		return "executable binary '" + getName() + "'";
	}
}
