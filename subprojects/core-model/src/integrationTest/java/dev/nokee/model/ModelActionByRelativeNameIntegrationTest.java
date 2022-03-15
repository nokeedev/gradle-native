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

import dev.nokee.model.internal.ElementNameComponent;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.actions.ModelSpec;
import dev.nokee.model.internal.actions.RelativeName;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import org.junit.jupiter.api.BeforeEach;

import static dev.nokee.model.internal.actions.ModelSpec.has;
import static dev.nokee.model.internal.core.ModelRegistration.builder;

class ModelActionByRelativeNameIntegrationTest extends ModelActionIntegrationTester {
	private final ModelNode ancestor = new ModelNode();
	private final ModelNode parent = new ModelNode();
	private final ModelNode unrelated = new ModelNode();

	@BeforeEach
	void createEntities() {
		ancestor.addComponent(new ElementNameComponent("orle"));
		parent.addComponent(new ParentComponent(ancestor));
		unrelated.addComponent(new ElementNameComponent("unre"));
	}

	@Override
	public ModelSpec spec() {
		return has(RelativeName.of(ancestor.getId(), "orleJkel"));
	}

	@Override
	public ModelRegistration newEntityMatchingSpec() {
		return builder().withComponent(ConfigurableTag.tag()).withComponent(new ElementNameComponent("jkel")).withComponent(new ParentComponent(parent)).build();
	}

	@Override
	public ModelRegistration newEntityNotMatchingSpec() {
		return builder().withComponent(ConfigurableTag.tag()).withComponent(new ElementNameComponent("leor")).withComponent(new ParentComponent(unrelated)).build();
	}
}
