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

import dev.nokee.model.KnownDomainObject;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;

import static dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureGroup;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public class CreateVariantAssembleLifecycleTaskRule implements Action<KnownDomainObject<? extends Variant>> {
	private final TaskRegistry taskRegistry;

	public CreateVariantAssembleLifecycleTaskRule(TaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void execute(KnownDomainObject<? extends Variant> knownVariant) {
		doExecute((VariantIdentifier) knownVariant.getIdentifier(), knownVariant.flatMap(TO_DEVELOPMENT_BINARY));
	}

	private void doExecute(VariantIdentifier variantIdentifier, Provider<Binary> binaryProvider) {
		// For single variant component, the task may already exists, coming from 'lifecycle-base'.
		val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), variantIdentifier), configureGroup(BUILD_GROUP));

		// Only multi-variant component should attach the proper dependency to the assemble task.
		//   Single variant depends on the a more complex logic around buildability, see CreateVariantAwareComponentAssembleLifecycleTaskRule
		if (!variantIdentifier.getAmbiguousDimensions().get().isEmpty()) {
			assembleTask.configure(configureDependsOn(binaryProvider));
		}
	}
}
