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

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ProjectMatchers.hasPlugin;
import static dev.nokee.scripts.testing.DefaultImporterMatchers.hasDefaultImportFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language-base")
class ObjectiveCppLanguageBasePluginIntegrationTest extends AbstractPluginTest {
	@Test
	void appliesLanguageBasePlugin() {
		assertThat(project, hasPlugin("dev.nokee.language-base"));
	}

	@Test
	void appliesStandardToolChainsPlugin() {
		assertThat(project.getPlugins(), hasItem(isA(NokeeStandardToolChainsPlugin.class)));
	}

	@Test
	void defaultImportSourceSetTypes() {
		assertThat(project, hasDefaultImportFor(ObjectiveCppSourceSet.class));
		assertThat(project, hasDefaultImportFor(CppHeaderSet.class));
		assertThat(project, hasDefaultImportFor(NativeHeaderSet.class));
	}
}
