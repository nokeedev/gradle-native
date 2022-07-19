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
package dev.nokee.platform.base;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.BaseNameComponent;
import dev.nokee.platform.base.internal.BaseNamePropertyComponent;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.model.internal.DomainObjectEntities.entityOf;
import static dev.nokee.model.internal.state.ModelStates.discover;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class HasBaseNameMixInIntegrationTest {
	ModelNode entity;

	@BeforeEach
	void createSubject(Project project) {
		entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelRegistration.builder()
			.withComponent(new ElementNameComponent("taqa"))
			.mergeFrom(entityOf(ModelBackedHasBaseNameMixIn.class))
			.build());
	}

	@Test
	void hasBaseNameTag() {
		assertTrue(entity.hasComponent(typeOf(ModelBackedHasBaseNameMixIn.Tag.class)));
	}

	@Test
	void hasBaseNamePropertyWhenDiscovered() {
		assertFalse(entity.has(BaseNamePropertyComponent.class));
		assertTrue(discover(entity).has(BaseNamePropertyComponent.class));
	}

	@Test
	void hasBaseNameWhenBucketFinalized() {
		assertFalse(entity.has(BaseNameComponent.class));
		assertFalse(discover(entity).has(BaseNameComponent.class));
		assertTrue(ModelStates.finalize(entity).has(BaseNameComponent.class));
	}

	@Nested
	class HasBaseNameProjectionTest implements HasBaseNameTester {
		ModelBackedHasBaseNameMixIn subject;

		@BeforeEach
		void realizeProjection() {
			subject = ModelNodeUtils.get(ModelStates.realize(entity), ModelBackedHasBaseNameMixIn.class);
		}

		@Override
		public ModelBackedHasBaseNameMixIn subject() {
			return subject;
		}
	}
}
