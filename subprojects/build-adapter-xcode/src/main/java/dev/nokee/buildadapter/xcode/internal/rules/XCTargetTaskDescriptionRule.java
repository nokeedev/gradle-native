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

import dev.nokee.buildadapter.xcode.internal.components.XCTargetComponent;
import dev.nokee.buildadapter.xcode.internal.components.XCTargetTaskComponent;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.platform.base.internal.tasks.TaskDescriptionComponent;

//ComponentFromEntity<DisplayNameComponent> read-only on ParentComponent
public final class XCTargetTaskDescriptionRule extends ModelActionWithInputs.ModelAction3<ParentComponent, XCTargetTaskComponent, XCTargetComponent> {
	@Override
	protected void execute(ModelNode entity, ParentComponent parent, XCTargetTaskComponent task, XCTargetComponent target) {
		assert parent.get().has(DisplayNameComponent.class) : "parent component must have display name";
		task.get().addComponent(new TaskDescriptionComponent(String.format("Builds target '%s' for %s.", target.get().getName(), parent.get().get(DisplayNameComponent.class).get())));
	}
}
