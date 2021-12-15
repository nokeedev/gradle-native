/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.model.internal.plugins;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.model.internal.core.ModelEntityFactory;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.Project;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(type = ModelBasePlugin.class)
class ModelBasePluginTest extends AbstractPluginTest {
	@Test
	void registersEventPublisherService() {
		assertThat(project, hasExtensionOf(DomainObjectEventPublisher.class));
	}

	@Test
	void registersRealizableService() {
		assertThat(project, hasExtensionOf(RealizableDomainObjectRealizer.class));
	}

	@Test
	void registersModelRegistryService() {
		assertThat(project, hasExtensionOf(ModelRegistry.class));
	}

	@Test
	void registersModelLookupService() {
		assertThat(project, hasExtensionOf(ModelLookup.class));
	}

	@Test
	void registersModelConfigurerService() {
		assertThat(project, hasExtensionOf(ModelConfigurer.class));
	}

	@Test
	void registersModelEntityFactoryService() {
		assertThat(project, hasExtensionOf(ModelEntityFactory.class));
	}

	private static Matcher<Project> hasExtensionOf(Class<?> extensionType) {
		return new TypeSafeMatcher<Project>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("extension of type " + extensionType);
			}

			@Override
			protected boolean matchesSafely(Project item) {
				return item.getExtensions().findByType(extensionType) != null;
			}
		};
	}
}
