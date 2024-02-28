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
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.nativebase.internal.HasOutputFile;
import dev.nokee.platform.nativebase.internal.NativeExecutableBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeVariantSpec;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
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

public final class NativeApplicationOutgoingDependencies implements NativeOutgoingDependencies {
	@Getter private final ConfigurableFileCollection exportedHeaders;
	@Getter private final RegularFileProperty exportedSwiftModule;
	@Getter private final Property<Binary> exportedBinary;

	public NativeApplicationOutgoingDependencies(NativeVariantSpec variant, Configuration runtimeElements, Project project) {
		this.exportedHeaders = project.getObjects().fileCollection();
		this.exportedSwiftModule = project.getObjects().fileProperty();
		this.exportedBinary = project.getObjects().property(Binary.class);

		final ModelObject<Sync> syncRuntimeLibraryTask = model(project, registryOf(Task.class)).register(variant.getIdentifier().child(TaskName.of("sync", "runtimeLibrary")), Sync.class);

		val runtimeLibraryName = finalizeValueOnRead(project.getObjects().property(String.class).value(project.provider(() -> {
			val toolChainSelector = new ToolChainSelectorInternal(((ProjectInternal) project).getModelRegistry());
			val platform = NativePlatformFactory.create(variant.getBuildVariant().getAxisValue(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS));
			val toolchain = toolChainSelector.select(platform);
			val toolProvider = toolchain.select(platform);
			val linkage = variant.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
			val baseName = ((HasBaseName)variant).getBaseName().get();
			if (linkage.isExecutable()) {
				return toolProvider.getExecutableName(baseName);
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

	private Provider<RegularFile> getOutgoingRuntimeLibrary(Binary binary) {
		if (binary instanceof NativeExecutableBinarySpec) {
			return ((NativeExecutableBinarySpec) binary).getLinkTask().flatMap(LinkExecutable::getLinkedFile);
		} else if (binary instanceof HasOutputFile) {
			return ((HasOutputFile) binary).getOutputFile();
		}
		throw new IllegalArgumentException("Unsupported binary to export");
	}
}
