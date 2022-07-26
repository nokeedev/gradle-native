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

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.VariantAwareComponentInternal;
import dev.nokee.utils.DeferUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;

import java.util.Arrays;

import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY;
import static dev.nokee.utils.RunnableUtils.onlyOnce;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureGroup;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public class CreateVariantAwareComponentAssembleLifecycleTaskRule implements Action<VariantAwareComponentInternal<?>> {
	private final ModelRegistry registry;

	public CreateVariantAwareComponentAssembleLifecycleTaskRule(ModelRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void execute(VariantAwareComponentInternal<?> component) {
		// The "component" assemble task was most likely added by the 'lifecycle-base' plugin
		//   then we configure the dependency.
		//   Note that the dependency may already exists for single variant component but it's not a big deal.
		val logger = new WarnUnbuildableLogger(component.getIdentifier());
		registry.register(newEntity(ASSEMBLE_TASK_NAME, Task.class, it -> it.ownedBy(ModelNodes.of(component)))).as(Task.class)
			.configure(configureGroup(BUILD_GROUP))
			.configure(configureDependsOn(component.getDevelopmentVariant().flatMap(TO_DEVELOPMENT_BINARY).map(Arrays::asList).orElse(DeferUtils.executes(onlyOnce(logger::warn)))));
	}
}
