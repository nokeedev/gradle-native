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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.internal.tasks.ObjectsLifecycleTask;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;

import java.util.Set;

import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class CreateVariantObjectsLifecycleTaskRule implements Action<KnownDomainObject<? extends Variant>> {
	private final TaskRegistry taskRegistry;

	public CreateVariantObjectsLifecycleTaskRule(TaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void execute(KnownDomainObject<? extends Variant> knownVariant) {
		doExecute((VariantIdentifier) knownVariant.getIdentifier(), knownVariant.flatMap(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS));
	}

	private void doExecute(VariantIdentifier variantIdentifier, Provider<Set<? extends SourceCompile>> compileTasksProvider) {
		taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of("objects"), ObjectsLifecycleTask.class, variantIdentifier)).configure(configureDependsOn(compileTasksProvider));
	}
}
