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

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDescription;

public class AssembleTaskCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	@Override
	public void apply(T target) {
		components(target).configureEach(new ConfigureAssembleTaskDescriptionRule<>());
		variants(target).configureEach(new ConfigureAssembleTaskDescriptionRule<>());

		components(target).configureEach(new ConfigureAssembleTaskGroupRule<>());
		variants(target).configureEach(new ConfigureAssembleTaskGroupRule<>());
	}

	private static final class ConfigureAssembleTaskDescriptionRule<TargetType> implements Action<TargetType> {
		@Override
		public void execute(TargetType target) {
			if (target instanceof HasAssembleTask) {
				((HasAssembleTask) target).getAssembleTask().configure(configureDescription("Assembles the outputs of the %s.", target));
			}
		}
	}

	private static final class ConfigureAssembleTaskGroupRule<TargetType> implements Action<TargetType> {
		@Override
		public void execute(TargetType target) {
			if (target instanceof HasAssembleTask) {
				((HasAssembleTask) target).getAssembleTask().configure(configureBuildGroup());
			}
		}
	}
}
