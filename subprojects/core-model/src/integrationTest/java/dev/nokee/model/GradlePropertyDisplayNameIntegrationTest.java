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

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.hasDisplayName;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static org.hamcrest.MatcherAssert.assertThat;

class GradlePropertyDisplayNameIntegrationTest {
	private final Project project = ProjectTestUtils.rootProject();
	private ModelRegistry registry;

	@BeforeEach
	void setUp() {
		project.getPluginManager().apply(ModelBasePlugin.class);
		registry = project.getExtensions().getByType(ModelRegistry.class);
	}

	@Nested
	class PropertyTest {
		private Property<String> subject;

		@BeforeEach
		void setUp() {
			registry.register(builder()
				.withComponent(new DisplayNameComponent(new DisplayName("my property")))
				.withComponent(new GradlePropertyComponent(subject = objectFactory().property(String.class)))
				.build());
		}

		@Test
		void hasDisplayNameMatchingDisplayNameComponent() {
			assertThat(subject, hasDisplayName("my property"));
		}
	}

	@Nested
	class SetPropertyTest {
		private SetProperty<String> subject;

		@BeforeEach
		void setUp() {
			registry.register(builder()
				.withComponent(new DisplayNameComponent(new DisplayName("my set property")))
				.withComponent(new GradlePropertyComponent(subject = objectFactory().setProperty(String.class)))
				.build());
		}

		@Test
		void hasDisplayNameMatchingDisplayNameComponent() {
			assertThat(subject, hasDisplayName("my set property"));
		}
	}

	@Nested
	class ListPropertyTest {
		private ListProperty<String> subject;

		@BeforeEach
		void setUp() {
			registry.register(builder()
				.withComponent(new DisplayNameComponent(new DisplayName("my list property")))
				.withComponent(new GradlePropertyComponent(subject = objectFactory().listProperty(String.class)))
				.build());
		}

		@Test
		void hasDisplayNameMatchingDisplayNameComponent() {
			assertThat(subject, hasDisplayName("my list property"));
		}
	}

	@Nested
	class RegularFilePropertyTest {
		private RegularFileProperty subject;

		@BeforeEach
		void setUp() {
			registry.register(builder()
				.withComponent(new DisplayNameComponent(new DisplayName("my file property")))
				.withComponent(new GradlePropertyComponent(subject = objectFactory().fileProperty()))
				.build());
		}

		@Test
		void hasDisplayNameMatchingDisplayNameComponent() {
			assertThat(subject, hasDisplayName("my file property"));
		}
	}

	@Nested
	class DirectoryPropertyTest {
		private DirectoryProperty subject;

		@BeforeEach
		void setUp() {
			registry.register(builder()
				.withComponent(new DisplayNameComponent(new DisplayName("my directory property")))
				.withComponent(new GradlePropertyComponent(subject = objectFactory().directoryProperty()))
				.build());
		}

		@Test
		void hasDisplayNameMatchingDisplayNameComponent() {
			assertThat(subject, hasDisplayName("my directory property"));
		}
	}
}
