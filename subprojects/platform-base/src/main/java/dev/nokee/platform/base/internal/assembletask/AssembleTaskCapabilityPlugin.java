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
package dev.nokee.platform.base.internal.assembletask;

import dev.nokee.model.internal.ModelObjects;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import java.util.function.BiConsumer;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDescription;

public class AssembleTaskCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	@Override
	public void apply(T target) {
		model(target, objects()).configureEach(HasAssembleTask.class, new ConfigureAssembleTaskDescriptionRule());
		model(target, objects()).configureEach(HasAssembleTask.class, new ConfigureAssembleTaskGroupRule());
	}

	private static final class ConfigureAssembleTaskDescriptionRule implements BiConsumer<ModelObjects.ModelObjectIdentity, HasAssembleTask> {
		@Override
		public void accept(ModelObjects.ModelObjectIdentity identifier, HasAssembleTask component) {
			component.getAssembleTask().configure(configureDescription("Assembles the outputs of the %s.", component));
		}
	}

	private static final class ConfigureAssembleTaskGroupRule implements BiConsumer<ModelObjects.ModelObjectIdentity, HasAssembleTask> {
		@Override
		public void accept(ModelObjects.ModelObjectIdentity identifier, HasAssembleTask target) {
			target.getAssembleTask().configure(configureBuildGroup());
		}
	}
}
