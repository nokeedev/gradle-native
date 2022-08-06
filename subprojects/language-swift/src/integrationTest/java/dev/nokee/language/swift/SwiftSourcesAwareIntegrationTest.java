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
package dev.nokee.language.swift;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.language.nativebase.internal.NativeSourcesAwareTag;
import dev.nokee.language.swift.internal.plugins.HasSwiftSourcesMixIn;
import dev.nokee.language.swift.internal.plugins.SupportSwiftSourceSetTag;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = SwiftLanguageBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class SwiftSourcesAwareIntegrationTest {
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
		void mixinSwiftSources() {
			assertTrue(subject.hasComponent(typeOf(HasSwiftSourcesMixIn.Tag.class)));
		}
	}

	@Nested
	class SupportOnSubjectEntityTest extends Tester {
		@BeforeEach
		void setUp() {
			subject.addComponent(tag(SupportSwiftSourceSetTag.class));
			subject.addComponent(tag(NativeSourcesAwareTag.class));
		}
	}

	@Nested
	class SupportOnDirectParentEntityTest extends Tester {
		@BeforeEach
		void setUp() {
			parent.addComponent(tag(SupportSwiftSourceSetTag.class));
			subject.addComponent(tag(NativeSourcesAwareTag.class));
		}
	}

	@Nested
	class SupportOnIndirectParentEntityTest extends Tester {
		@BeforeEach
		void setUp() {
			grandParent.addComponent(tag(SupportSwiftSourceSetTag.class));
			subject.addComponent(tag(NativeSourcesAwareTag.class));
		}
	}
}
