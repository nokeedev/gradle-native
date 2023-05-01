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
package dev.nokee.model;

import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.actions.ModelSpec;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import org.junit.jupiter.api.BeforeEach;

import static dev.nokee.model.fixtures.ModelEntityTestUtils.newEntity;
import static dev.nokee.model.internal.actions.ModelSpec.descendantOf;
import static dev.nokee.model.internal.core.ModelRegistration.builder;

class ModelActionByAncestorsIntegrationTest extends ModelActionIntegrationTester {
	private final ModelNode ancestor = newEntity();
	private final ModelNode parent = newEntity();
	private final ModelNode unrelated = newEntity();

	@BeforeEach
	void createEntities() {
		parent.addComponent(new ParentComponent(ancestor));
	}

	@Override
	public ModelSpec spec() {
		return descendantOf(ancestor.getId());
	}

	@Override
	public ModelRegistration newEntityMatchingSpec() {
		return builder().withComponentTag(ConfigurableTag.class).withComponent(new ParentComponent(parent)).build();
	}

	@Override
	public ModelRegistration newEntityNotMatchingSpec() {
		return builder().withComponentTag(ConfigurableTag.class).withComponent(new ParentComponent(unrelated)).build();
	}
}
