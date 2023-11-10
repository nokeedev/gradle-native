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

import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMap;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.runtime.nativebase.TargetMachine;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.lockProperty;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

final class ConfigureLinkTaskTargetPlatformFromBuildVariantRule implements Action<Variant> {
	private final ModelMap<Task> tasks;

	public ConfigureLinkTaskTargetPlatformFromBuildVariantRule(ModelMap<Task> tasks) {
		this.tasks = tasks;
	}

	@Override
	public void execute(Variant variant) {
		ModelElementSupport.safeAsModelElement(variant).map(ModelElement::getIdentifier).ifPresent(variantIdentifier -> {
			tasks.configureEach(AbstractLinkTask.class, task -> {
				ModelElementSupport.safeAsModelElement(task).map(ModelElement::getIdentifier).ifPresent(taskIdentifier -> {
					if (ModelObjectIdentifiers.descendantOf(taskIdentifier, variantIdentifier)) {
						configureTargetPlatform(set(fromBuildVariant(variant.getBuildVariant())).andThen(lockProperty())).execute(task);
					}
				});
			});
		});
	}

	//region Target platform
	private static <SELF extends Task> Action<SELF> configureTargetPlatform(BiConsumer<? super SELF, ? super PropertyUtils.Property<? extends NativePlatform>> action) {
		return task -> {
			action.accept(task, wrap(targetPlatformProperty(task)));
		};
	}

	private static NativePlatform fromBuildVariant(BuildVariant buildVariant) {
		return NativePlatformFactory.create(((BuildVariantInternal) buildVariant).getAxisValue(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS));
	}

	private static Property<NativePlatform> targetPlatformProperty(Task task) {
		if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getTargetPlatform();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion
}
