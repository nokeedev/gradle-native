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
package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.model.internal.ModelObject;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.HasOutputFile;
import dev.nokee.platform.nativebase.internal.NativeSharedLibraryBinarySpec;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.TaskUtils;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;

import java.io.File;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;

public abstract class AbstractNativeLibraryOutgoingDependencies {
	@Getter private final ConfigurableFileCollection exportedHeaders;
	@Getter private final RegularFileProperty exportedSwiftModule;
	@Getter private final Property<Binary> exportedBinary;
	private final Configuration linkElements;
	private final Configuration runtimeElements;

	protected AbstractNativeLibraryOutgoingDependencies(VariantIdentifier variantIdentifier, Configuration linkElements, Configuration runtimeElements, Project project, Provider<String> exportBaseName) {
		this.exportedHeaders = project.getObjects().fileCollection();
		this.exportedSwiftModule = project.getObjects().fileProperty();
		this.exportedBinary = project.getObjects().property(Binary.class);
		this.linkElements = linkElements;
		this.runtimeElements = runtimeElements;

		final BuildVariantInternal buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		final ModelObject<Sync> syncLinkLibraryTask = model(project, registryOf(Task.class)).register(variantIdentifier.child(TaskName.of("sync", "linkLibrary")), Sync.class);
		val linkLibraryName = finalizeValueOnRead(project.getObjects().property(String.class).value(project.provider(() -> {
			val toolChainSelector = new ToolChainSelectorInternal(((ProjectInternal) project).getModelRegistry());
			val platform = NativePlatformFactory.create(buildVariant.getAxisValue(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS));
			val toolchain = toolChainSelector.select(platform);
			val toolProvider = toolchain.select(platform);
			val linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
			val baseName = exportBaseName.get();
			if (linkage.isStatic()) {
				return toolProvider.getStaticLibraryName(baseName);
			} else if (linkage.isShared()) {
				if (toolProvider.producesImportLibrary()) {
					return toolProvider.getImportLibraryName(baseName);
				} else {
					return toolProvider.getSharedLibraryLinkFileName(baseName);
				}
			} else {
				throw new UnsupportedOperationException();
			}
		})));
		syncLinkLibraryTask.configure(task -> {
			task.from(getExportedBinary().flatMap(this::getOutgoingLinkLibrary), spec -> spec.rename(it -> linkLibraryName.get()));
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir(TaskUtils.temporaryDirectoryPath(task)).get().getAsFile());
		});
		linkElements.getOutgoing().artifact(syncLinkLibraryTask.asProvider().map(it -> new File(it.getDestinationDir(), linkLibraryName.get())));


		final ModelObject<Sync> syncRuntimeLibraryTask = model(project, registryOf(Task.class)).register(variantIdentifier.child(TaskName.of("sync", "runtimeLibrary")), Sync.class);
		if (!buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).isStatic()) {
			val runtimeLibraryName = finalizeValueOnRead(project.getObjects().property(String.class).value(project.provider(() -> {
				val toolChainSelector = new ToolChainSelectorInternal(((ProjectInternal) project).getModelRegistry());
				val platform = NativePlatformFactory.create(buildVariant.getAxisValue(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS));
				val toolchain = toolChainSelector.select(platform);
				val toolProvider = toolchain.select(platform);
				val linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
				val baseName = exportBaseName.get();
				if (linkage.isShared()) {
					return toolProvider.getSharedLibraryName(baseName);
				} else {
					throw new UnsupportedOperationException();
				}
			})));
			syncRuntimeLibraryTask.configure(task -> {
				task.from(getExportedBinary().flatMap(this::getOutgoingRuntimeLibrary), spec -> spec.rename(it -> runtimeLibraryName.get()));
				task.setDestinationDir(project.getLayout().getBuildDirectory().dir(TaskUtils.temporaryDirectoryPath(task)).get().getAsFile());
			});
			runtimeElements.getOutgoing().artifact(syncRuntimeLibraryTask.asProvider().map(it -> new File(it.getDestinationDir(), runtimeLibraryName.get())));
		}
	}

	public Configuration getLinkElements() {
		return linkElements;
	}

	public Configuration getRuntimeElements() {
		return runtimeElements;
	}

	private Provider<RegularFile> getOutgoingLinkLibrary(Binary binary) {
		if (binary instanceof NativeSharedLibraryBinarySpec) {
			return ((NativeSharedLibraryBinarySpec) binary).getLinkTask().flatMap(it -> it.getImportLibrary().orElse(it.getLinkedFile()));
		} else if (binary instanceof StaticLibraryBinary) {
			return ((StaticLibraryBinary) binary).getCreateTask().flatMap(CreateStaticLibrary::getOutputFile);
		} else if (binary instanceof HasOutputFile) {
			return ((HasOutputFile) binary).getOutputFile();
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}

	private Provider<RegularFile> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof NativeSharedLibraryBinarySpec) {
			return ((NativeSharedLibraryBinarySpec) binary).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile);
		} else if (binary instanceof StaticLibraryBinary) {
			throw new UnsupportedOperationException();
		} else if (binary instanceof HasOutputFile) {
			return ((HasOutputFile) binary).getOutputFile();
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}
}
