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
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import org.junit.jupiter.api.BeforeEach;

import static dev.nokee.model.internal.actions.ModelActionSystem.updateSelectorForTag;
import static dev.nokee.model.internal.actions.ModelSpec.isEqual;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;

class ModelActionByTagIntegrationTest extends ModelActionIntegrationTester {
	@BeforeEach
	void registerUpdateRule() {
		registry().configure(updateSelectorForTag(MyTagComponent.class));
	}

	@Override
	public ModelSpec spec() {
		return isEqual(MyTagComponent.tag());
	}

	@Override
	public ModelRegistration newEntityMatchingSpec() {
		return builder().withComponent(tag(ConfigurableTag.class)).withComponent(MyTagComponent.tag()).build();
	}

	@Override
	public ModelRegistration newEntityNotMatchingSpec() {
		return builder().withComponent(tag(ConfigurableTag.class)).build();
	}

	private static final class MyTagComponent implements ModelComponent {
		private static final MyTagComponent INSTANCE = new MyTagComponent();

		private MyTagComponent() {}

		private static MyTagComponent tag() {
			return INSTANCE;
		}
	}
}
