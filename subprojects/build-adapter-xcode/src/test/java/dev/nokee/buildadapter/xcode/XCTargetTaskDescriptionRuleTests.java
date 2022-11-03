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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.components.XCTargetTaskComponent;
import dev.nokee.buildadapter.xcode.internal.rules.XCTargetTaskDescriptionRule;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.internal.tasks.TaskDescriptionComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class XCTargetTaskDescriptionRuleTests {
	ModelAction subject = new XCTargetTaskDescriptionRule();
	ModelNode target;
	ModelNode task;

	@BeforeEach
	void createEntities() {
		task = new ModelNode();

		target = new ModelNode();
		target.addComponent(new DisplayNameComponent("my awesome target"));
		target.addComponent(new XCTargetTaskComponent(task));
	}

	@Test
	void addsTaskDescriptionOnTargetTaskReferencingEntityDisplayName() {
		subject.execute(target);

		assertThat(task.get(TaskDescriptionComponent.class).get(), equalTo("Builds my awesome target."));
	}
}
