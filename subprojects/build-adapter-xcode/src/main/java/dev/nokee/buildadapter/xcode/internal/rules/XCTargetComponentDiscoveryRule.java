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

import dev.nokee.buildadapter.xcode.internal.plugins.XCProjectAdapterSpec;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import org.gradle.api.Action;
import org.gradle.api.Project;

public final class XCTargetComponentDiscoveryRule implements Action<Project> {
	private final ModelObjectRegistry<Component> componentRegistry;
	private final XCLoader<Iterable<XCTargetReference>, XCProjectReference> targetLoader;

	public XCTargetComponentDiscoveryRule(ModelObjectRegistry<Component> componentRegistry, XCLoader<Iterable<XCTargetReference>, XCProjectReference> targetLoader) {
		this.componentRegistry = componentRegistry;
		this.targetLoader = targetLoader;
	}

	@Override
	public void execute(Project project) {
		project.getExtensions().getByType(XCProjectReference.class).load(targetLoader).forEach(target -> {
			componentRegistry.register(ProjectIdentifier.of(project).child(target.getName()), XCProjectAdapterSpec.class)
				.configure(it -> it.getTarget().set(target));
		});
	}
}
