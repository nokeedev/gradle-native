/*
 * Copyright 2021 the original author or authors.
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
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetRegistrationFactory;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;

@PluginRequirement.Require(id = "dev.nokee.cpp-language-base")
class CppSourceSetIntegrationTest extends AbstractPluginTest {
	private CppSourceSet subject;

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(CppSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "zomi"), false)).as(CppSourceSet.class).get();
	}

	@Nested
	class SourceSetTest extends CppSourceSetIntegrationTester {
		@BeforeEach
		public void configureTargetPlatform() {
			((CppCompileTask) project.getTasks().getByName("compileZomi")).getTargetPlatform().set(create(of("macos-x64")));
		}

		@Override
		public CppSourceSet subject() {
			return subject;
		}

		@Override
		public Project project() {
			return project;
		}

		@Override
		public String name() {
			return "zomi";
		}

		@Override
		public String variantName() {
			return "zomi";
		}

		@Override
		public String displayName() {
			return "sources ':zomi'";
		}
	}
}
