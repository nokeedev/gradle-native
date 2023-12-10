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

import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.utils.TextCaseUtils;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;

public final class SwiftLibraryOutgoingDependencies extends AbstractNativeLibraryOutgoingDependencies implements NativeOutgoingDependencies {
	public SwiftLibraryOutgoingDependencies(VariantIdentifier variantIdentifier, Configuration apiElements, Configuration linkElements, Configuration runtimeElements, Project project, Provider<String> exportedBaseName) {
		super(variantIdentifier, linkElements, runtimeElements, project, exportedBaseName);

		val syncTask = model(project, registryOf(Task.class)).register(variantIdentifier.child(TaskName.of("sync", "importModule")), Sync.class);
		syncTask.configure(task -> {
			task.from(getExportedSwiftModule(), spec -> spec.rename(it -> TextCaseUtils.toCamelCase(exportedBaseName.get()) + ".swiftmodule"));
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task)).get().getAsFile());
		});
		apiElements.getOutgoing().artifact(syncTask.asProvider().map(Sync::getDestinationDir));
	}
}
