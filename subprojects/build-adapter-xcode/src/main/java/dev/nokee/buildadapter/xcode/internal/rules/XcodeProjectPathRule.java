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

import dev.nokee.buildadapter.xcode.internal.GradleProjectPathService;
import dev.nokee.buildadapter.xcode.internal.components.GradleProjectPathComponent;
import dev.nokee.buildadapter.xcode.internal.components.GradleProjectTag;
import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.tags.ModelComponentTag;

public final class XcodeProjectPathRule extends ModelActionWithInputs.ModelAction2<ModelComponentTag<GradleProjectTag>, XCProjectComponent> {
	private final GradleProjectPathService projectPaths;

	public XcodeProjectPathRule(GradleProjectPathService projectPaths) {
		this.projectPaths = projectPaths;
	}

	@Override
	protected void execute(ModelNode entity, ModelComponentTag<GradleProjectTag> ignore1, XCProjectComponent projectReference) {
		entity.addComponent(new GradleProjectPathComponent(projectPaths.toProjectPath(projectReference.get())));
	}
}
