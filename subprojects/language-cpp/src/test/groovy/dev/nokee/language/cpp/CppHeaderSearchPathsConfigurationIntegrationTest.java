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
package dev.nokee.language.cpp;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.Subject;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetRegistrationFactory;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

@PluginRequirement.Require(id = "dev.nokee.cpp-language-base")
@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
class CppHeaderSearchPathsConfigurationIntegrationTest extends AbstractPluginTest {
	@Subject Configuration subject;

	Configuration createSubject() {
		return project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(CppSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "zare"))).element("headerSearchPaths", Configuration.class).get();
	}

	@Test
	void hasName() {
		assertThat(subject, named("zareHeaderSearchPaths"));
	}

	@Test
	void isResolvable() {
		assertThat(subject, ConfigurationMatchers.resolvable());
	}

	@Test
	void hasCPlusPlusApiUsage() {
		assertThat(subject, ConfigurationMatchers.attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("cplusplus-api"))));
	}

	@Test
	void hasDescription() {
		assertThat(subject, ConfigurationMatchers.description("Header search paths for sources ':zare'."));
	}
}
