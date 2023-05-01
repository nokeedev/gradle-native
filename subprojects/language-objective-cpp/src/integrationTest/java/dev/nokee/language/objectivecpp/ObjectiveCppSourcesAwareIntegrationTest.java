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
package dev.nokee.language.objectivecpp;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.language.nativebase.internal.HasPrivateHeadersMixIn;
import dev.nokee.language.nativebase.internal.NativeSourcesAwareTag;
import dev.nokee.language.objectivecpp.internal.HasObjectiveCppSourcesMixIn;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.language.objectivecpp.internal.plugins.SupportObjectiveCppSourceSetTag;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ObjectiveCppLanguageBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class ObjectiveCppSourcesAwareIntegrationTest {
	ModelNode grandParent;
	ModelNode parent;
	ModelNode subject;

	@BeforeEach
	void createSubject(Project project) {
		grandParent = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder().build());
		parent = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(new ParentComponent(grandParent))
			.build());
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(new ParentComponent(parent))
			.build());
	}

	abstract class Tester {
		@Test
		void mixinCSources() {
			assertTrue(subject.hasComponent(typeOf(HasObjectiveCppSourcesMixIn.Tag.class)));
		}

		@Test
		void mixinPrivateHeaders() {
			assertTrue(subject.hasComponent(typeOf(HasPrivateHeadersMixIn.Tag.class)));
		}
	}

	@Nested
	class SupportOnSubjectEntityTest extends Tester {
		@BeforeEach
		void setUp() {
			subject.addComponentTag(SupportObjectiveCppSourceSetTag.class);
			subject.addComponentTag(NativeSourcesAwareTag.class);
		}
	}

	@Nested
	class SupportOnDirectParentEntityTest extends Tester {
		@BeforeEach
		void setUp() {
			parent.addComponentTag(SupportObjectiveCppSourceSetTag.class);
			subject.addComponentTag(NativeSourcesAwareTag.class);
		}
	}

	@Nested
	class SupportOnIndirectParentEntityTest extends Tester {
		@BeforeEach
		void setUp() {
			grandParent.addComponentTag(SupportObjectiveCppSourceSetTag.class);
			subject.addComponentTag(NativeSourcesAwareTag.class);
		}
	}
}
