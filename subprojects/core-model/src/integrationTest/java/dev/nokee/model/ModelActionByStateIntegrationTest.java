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
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.state.ModelState;

import static dev.nokee.model.internal.actions.ModelSpec.stateAtLeast;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;

class ModelActionByStateIntegrationTest extends ModelActionIntegrationTester {
	@Override
	public ModelSpec spec() {
		return stateAtLeast(ModelState.Realized);
	}

	@Override
	public ModelRegistration newEntityMatchingSpec() {
		return builder().withComponent(tag(ConfigurableTag.class)).withComponent(ModelState.Realized).build();
	}

	@Override
	public ModelRegistration newEntityNotMatchingSpec() {
		return builder().withComponent(tag(ConfigurableTag.class)).withComponent(ModelState.Created).build();
	}
}
