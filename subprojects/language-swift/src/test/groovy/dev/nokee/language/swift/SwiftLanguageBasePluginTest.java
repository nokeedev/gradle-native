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
package dev.nokee.language.swift;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ProjectMatchers.hasPlugin;
import static dev.nokee.scripts.testing.DefaultImporterMatchers.hasDefaultImportFor;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(id = "dev.nokee.swift-language-base")
class SwiftLanguageBasePluginTest extends AbstractPluginTest {
	@Test
	void appliesLanguageBasePlugin() {
		assertThat(project, hasPlugin("dev.nokee.language-base"));
	}

	@Test
	void appliesSwiftcToolChainsPlugin() {
		assertThat(project, hasPlugin(SwiftCompilerPlugin.class));
	}

	@Test
	void defaultImportSourceSetTypes() {
		assertThat(project, hasDefaultImportFor(SwiftSourceSet.class));
	}
}
