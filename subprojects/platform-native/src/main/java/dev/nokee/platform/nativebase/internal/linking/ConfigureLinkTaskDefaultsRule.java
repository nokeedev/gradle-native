/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.linking;

import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import org.gradle.api.Action;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.base.internal.util.PropertyUtils.lockProperty;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

final class ConfigureLinkTaskDefaultsRule implements Action<Artifact> {
	@Override
	public void execute(Artifact target) {
		if (target instanceof SharedLibraryBinary) {
			((SharedLibraryBinary) target).getLinkTask().configure(task -> {
				if (task instanceof LinkSharedLibraryTask) {
					configureInstallName(convention(ofLinkedFileFileName())).execute((LinkSharedLibraryTask) task);
				}
			});
			((SharedLibraryBinary) target).getLinkTask().configure(task -> {
				if (task instanceof LinkSharedLibraryTask) {
					configureImportLibrary(lockProperty()).execute((LinkSharedLibraryTask) task);
				}
			}); // Already has sensible default in task implementation
		}
	}

	//region Install name
	private static Action<LinkSharedLibraryTask> configureInstallName(BiConsumer<? super LinkSharedLibraryTask, ? super PropertyUtils.Property<String>> action) {
		return task -> action.accept(task, wrap(task.getInstallName()));
	}

	private static Function<LinkSharedLibraryTask, Provider<String>> ofLinkedFileFileName() {
		return task -> task.getLinkedFile().getLocationOnly().map(linkedFile -> linkedFile.getAsFile().getName());
	}
	//endregion

	//region Import library
	private static Action<LinkSharedLibraryTask> configureImportLibrary(BiConsumer<? super LinkSharedLibraryTask, ? super PropertyUtils.Property<? extends RegularFile>> action) {
		return task -> action.accept(task, wrap(task.getImportLibrary()));
	}
	//endregion
}
