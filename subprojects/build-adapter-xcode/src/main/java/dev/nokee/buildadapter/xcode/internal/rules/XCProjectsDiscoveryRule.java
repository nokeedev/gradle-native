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
package dev.nokee.buildadapter.xcode.internal.rules;

import dev.nokee.buildadapter.xcode.internal.components.GradleProjectTag;
import dev.nokee.buildadapter.xcode.internal.components.GradleSettingsTag;
import dev.nokee.buildadapter.xcode.internal.components.SettingsDirectoryComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.buildadapter.xcode.internal.plugins.XCProjectLocator;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelComponentTag;
import lombok.val;

public final class XCProjectsDiscoveryRule extends ModelActionWithInputs.ModelAction2<SettingsDirectoryComponent, ModelComponentTag<GradleSettingsTag>> {
	private final ModelRegistry registry;
	private final XCProjectLocator locator;

	public XCProjectsDiscoveryRule(ModelRegistry registry, XCProjectLocator locator) {
		this.registry = registry;
		this.locator = locator;
	}

	@Override
	protected void execute(ModelNode entity, SettingsDirectoryComponent settingsDirectory, ModelComponentTag<GradleSettingsTag> ignored) {
		val actualProjects = locator.findProjects(settingsDirectory.get());

		actualProjects.forEach(project -> {
			registry.instantiate(ModelRegistration.builder().withComponent(new ParentComponent(entity)).withComponentTag(GradleProjectTag.class).withComponent(new XCProjectComponent(project)).build());
		});
	}
}
