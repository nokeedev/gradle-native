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

import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCTargetComponent;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;

import static dev.nokee.model.internal.tags.ModelTags.tag;

public final class XCTargetComponentDiscoveryRule extends ModelActionWithInputs.ModelAction2<XCProjectComponent, DisplayNameComponent> {
	private final ModelRegistry registry;
	private final XCLoader<Iterable<XCTargetReference>, XCProjectReference> targetLoader;

	public XCTargetComponentDiscoveryRule(ModelRegistry registry, XCLoader<Iterable<XCTargetReference>, XCProjectReference> targetLoader) {
		this.registry = registry;
		this.targetLoader = targetLoader;
	}

	@Override
	protected void execute(ModelNode entity, XCProjectComponent xcProject, DisplayNameComponent displayName) {
		xcProject.get().load(targetLoader).forEach(target -> {
			ModelStates.register(registry.instantiate(ModelRegistration.builder()
				.withComponent(new ElementNameComponent(target.getName()))
				.withComponent(new XCTargetComponent(target))
				.withComponent(tag(IsComponent.class))
				.withComponent(new DisplayNameComponent(String.format("target '%s' of %s", target.getName(), displayName.get())))
				.withComponent(new ParentComponent(entity))
				.build()));
		});
	}
}
