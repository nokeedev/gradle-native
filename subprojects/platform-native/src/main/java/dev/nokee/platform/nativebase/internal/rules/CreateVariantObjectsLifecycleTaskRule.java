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
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.internal.tasks.ObjectsLifecycleTask;
import org.gradle.api.Action;
import org.gradle.api.provider.Provider;

import java.util.Set;

import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class CreateVariantObjectsLifecycleTaskRule implements Action<KnownDomainObject<? extends Variant>> {
	private final ModelRegistry registry;

	public CreateVariantObjectsLifecycleTaskRule(ModelRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void execute(KnownDomainObject<? extends Variant> knownVariant) {
		doExecute(ModelNodes.of(knownVariant), knownVariant.flatMap(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS));
	}

	private void doExecute(ModelNode entity, Provider<Set<? extends SourceCompile>> compileTasksProvider) {
		registry.register(newEntity("objects", ObjectsLifecycleTask.class, it -> it.ownedBy(entity))).configure(ObjectsLifecycleTask.class, configureDependsOn(compileTasksProvider));
	}
}
