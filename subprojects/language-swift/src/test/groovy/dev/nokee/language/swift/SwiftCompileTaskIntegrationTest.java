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
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetRegistrationFactory;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.artifacts.Configuration;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(id = "dev.nokee.swift-language-base")
class SwiftCompileTaskIntegrationTest extends AbstractPluginTest {
	private SwiftCompileTask subject;

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(SwiftSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "rubi"))).element("compile", SwiftCompileTask.class).get();
	}

	@Test
	void hasName() {
		assertThat(subject, named("compileRubi"));
	}

	@Test
	void hasDescription() {
		assertThat(subject, TaskMatchers.description("Compiles the sources ':rubi'."));
	}
}
