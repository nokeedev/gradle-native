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
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.BaseNameComponent;
import dev.nokee.platform.base.internal.BaseNamePropertyComponent;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.developmentbinary.DevelopmentBinaryComponent;
import dev.nokee.platform.base.internal.developmentbinary.DevelopmentBinaryPropertyComponent;
import dev.nokee.platform.base.internal.developmentbinary.HasDevelopmentBinaryMixIn;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import dev.nokee.platform.base.testers.HasDevelopmentBinaryTester;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.model.internal.state.ModelStates.discover;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.platform.base.internal.DomainObjectEntities.entityOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class HasDevelopmentBinaryMixInIntegrationTest {
	ModelNode entity;

	@BeforeEach
	void createSubject(Project project) {
		entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelRegistration.builder()
			.withComponent(new ElementNameComponent("taqa"))
			.mergeFrom(entityOf(HasDevelopmentBinaryMixIn.class))
			.build());
	}

	@Test
	void hasDevelopmentBinaryTag() {
		assertTrue(entity.hasComponent(typeOf(HasDevelopmentBinaryMixIn.Tag.class)));
	}

	@Test
	void hasDevelopmentBinaryPropertyWhenDiscovered() {
		assertFalse(entity.has(DevelopmentBinaryPropertyComponent.class));
		assertTrue(discover(entity).has(DevelopmentBinaryPropertyComponent.class));
	}

	@Test
	void hasDevelopmentBinaryWhenFinalized() {
		assertFalse(entity.has(DevelopmentBinaryComponent.class));
		assertFalse(discover(entity).has(DevelopmentBinaryComponent.class));
		assertTrue(ModelStates.finalize(entity).has(DevelopmentBinaryComponent.class));
	}

	@Nested
	class HasDevelopmentBinaryProjectionTest implements HasDevelopmentBinaryTester {
		HasDevelopmentBinaryMixIn subject;

		@BeforeEach
		void realizeProjection() {
			subject = ModelNodeUtils.get(ModelStates.realize(entity), HasDevelopmentBinaryMixIn.class);
		}

		@Override
		public HasDevelopmentBinaryMixIn subject() {
			return subject;
		}
	}
}
