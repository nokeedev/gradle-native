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

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.Subject;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetSpec;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.model.internal.DomainObjectEntities.newEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

@PluginRequirement.Require(id = "dev.nokee.swift-language-base")
class SwiftSourceSetImportModulesConfigurationIntegrationTest extends AbstractPluginTest {
	@Subject Configuration subject;

	Configuration createSubject() {
		return project.getExtensions().getByType(ModelRegistry.class).register(newEntity("cavu", SwiftSourceSetSpec.class)).element("importModules", Configuration.class).get();
	}

	@Test
	void hasName() {
		assertThat(subject, named("cavuImportModules"));
	}

	@Test
	void isResolvable() {
		assertThat(subject, ConfigurationMatchers.resolvable());
	}

	@Test
	void hasSwiftApiUsage() {
		assertThat(subject, ConfigurationMatchers.attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("swift-api"))));
	}

	@Test
	void hasDescription() {
		assertThat(subject, ConfigurationMatchers.description("Import modules for sources ':cavu'."));
	}
}
