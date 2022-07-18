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
import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.internal.DefaultHeaderSearchPath;
import dev.nokee.language.nativebase.internal.ObjectSourceSet;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.linking.HasLinkTask;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkTask;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.TaskDependencyUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SharedLibraryBinaryInternal extends BaseNativeBinary implements SharedLibraryBinary
	, Buildable
	, HasPublicType
	, ModelBackedNamedMixIn
	, ModelBackedHasBaseNameMixIn
	, HasLinkTask<LinkSharedLibrary, LinkSharedLibraryTask>
	, HasObjectFilesToBinaryTask
{
	private final NativeIncomingDependencies dependencies;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providerFactory;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter RegularFileProperty linkedFile;

	// TODO: The dependencies passed over here should be a read-only like only FileCollections
	@Inject
	public SharedLibraryBinaryInternal(BinaryIdentifier identifier, TargetMachine targetMachine, DomainObjectSet<ObjectSourceSet> objectSourceSets, NativeIncomingDependencies dependencies, ObjectFactory objects, ProjectLayout layout, ProviderFactory providers, TaskContainer tasks, TaskView<Task> compileTasks) {
		super(identifier, objectSourceSets, targetMachine, dependencies, objects, layout, providers, compileTasks);
		this.dependencies = dependencies;
		this.objects = objects;
		this.providerFactory = providers;
		this.tasks = tasks;
		this.linkedFile = objects.fileProperty();

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

			task.getLinkerArgs().addAll(task.getToolChain().map(it -> {
				if (it instanceof Swiftc) {
					return ImmutableList.of("-module-name", toModuleName(getBaseName().get()));
				}
				return ImmutableList.of();
			}));
		});
		getCreateOrLinkTask().configure(this::configureSharedLibraryTask);

		getLinkedFile().set(getCreateOrLinkTask().flatMap(AbstractLinkTask::getLinkedFile));
		getLinkedFile().disallowChanges();
	}

	private String toModuleName(String baseName) {
		return GUtil.toCamelCase(baseName);
	}

	private void configureSharedLibraryTask(LinkSharedLibraryTask task) {
		task.setDescription("Links the shared library.");
		task.source(getObjectFiles());

		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		// Until we model the build type
		task.getDebuggable().set(false);

		Provider<String> installName = task.getLinkedFile().getLocationOnly().map(linkedFile -> linkedFile.getAsFile().getName());
		task.getInstallName().set(installName);

		task.getDestinationDirectory().convention(getLayout().getBuildDirectory().dir(identifier.getOutputDirectoryBase("libs")));
		task.getLinkedFile().convention(getSharedLibraryLinkedFile());
	}

	private Provider<RegularFile> getSharedLibraryLinkedFile() {
		return getLayout().getBuildDirectory().file(getBaseName().map(it -> {
			OperatingSystemFamily osFamily = getTargetMachine().getOperatingSystemFamily();
			OperatingSystemOperations osOperations = OperatingSystemOperations.of(osFamily);
			return osOperations.getSharedLibraryName(identifier.getOutputDirectoryBase("libs") + "/" + it);
		}));
	}

	@Override
	public TaskProvider<LinkSharedLibrary> getLinkTask() {
		return (TaskProvider<LinkSharedLibrary>) ModelElements.of(this, NativeLinkTask.class).as(LinkSharedLibrary.class).asProvider();
	}

	@Override
	public TaskProvider<LinkSharedLibraryTask> getCreateOrLinkTask() {
		return (TaskProvider<LinkSharedLibraryTask>) ModelElements.of(this, NativeLinkTask.class).as(LinkSharedLibraryTask.class).asProvider();
	}

	@Override
	public boolean isBuildable() {
		try {
			return super.isBuildable() && isBuildable(getCreateOrLinkTask().get());
		} catch (Throwable ex) { // because toolchain selection calls xcrun for macOS which doesn't exists on non-mac system
			return false;
		}
	}

	private static boolean isBuildable(LinkSharedLibrary linkTask) {
		AbstractLinkTask linkTaskInternal = (AbstractLinkTask)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	private static List<HeaderSearchPath> toHeaderSearchPaths(Set<FileSystemLocation> paths) {
		return paths.stream().map(FileSystemLocation::getAsFile).map(DefaultHeaderSearchPath::new).collect(Collectors.toList());
	}

	private Stream<String> toFrameworkFlags(File it) {
		return ImmutableList.of("-F", it.getParent(), "-framework", FilenameUtils.removeExtension(it.getName())).stream();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return TaskDependencyUtils.of(getCreateOrLinkTask());
	}

	public FileCollection getRuntimeLibrariesDependencies() {
		return dependencies.getRuntimeLibraries();
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(SharedLibraryBinary.class);
	}

	@Override
	public String toString() {
		return "shared library binary '" + getName() + "'";
	}
}
