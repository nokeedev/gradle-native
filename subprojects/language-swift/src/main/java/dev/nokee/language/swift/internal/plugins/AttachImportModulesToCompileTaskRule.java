/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.nativebase.internal.NativeCompileTask;
import dev.nokee.language.swift.tasks.SwiftCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;

import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

public final class AttachImportModulesToCompileTaskRule extends ModelActionWithInputs.ModelAction4<LanguageSourceSetIdentifier, ModelState.IsAtLeastRealized, DependentImportModules, NativeCompileTask> {
	private final LanguageSourceSetIdentifier identifier;

	public AttachImportModulesToCompileTaskRule(LanguageSourceSetIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	protected void execute(ModelNode entity, LanguageSourceSetIdentifier identifier, ModelState.IsAtLeastRealized isAtLeastRealized, DependentImportModules incomingModules, NativeCompileTask compileTask) {
		if (identifier.equals(this.identifier)) {
			compileTask.configure(SwiftCompile.class, configureImportModules(from(incomingModules)));
		}
	}

	//region Import modules
	public static Action<Task> configureImportModules(BiConsumer<? super Task, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(importModulesProperty(task)));
	}

	private static ConfigurableFileCollection importModulesProperty(Task task) {
		if (task instanceof SwiftCompileTask) {
			return ((SwiftCompileTask) task).getModules();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion
}
