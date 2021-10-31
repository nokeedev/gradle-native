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
package dev.nokee.language.c;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.internal.plugins.CSourceSetRegistrationFactory;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.FileSystemMatchers.*;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@PluginRequirement.Require(id = "dev.nokee.c-language-base")
class CSourceSetIntegrationTest extends AbstractPluginTest {
	private CSourceSet subject;

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(CSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "nopu"), false)).as(CSourceSet.class).get();
	}

	@Nested
	class SourceSetTest extends CSourceSetIntegrationTester {
		@BeforeEach
		public void configureTargetPlatform() {
			((CCompileTask) project.getTasks().getByName("compileNopu")).getTargetPlatform().set(create(of("macos-x64")));
		}

		@Override
		public CSourceSet subject() {
			return subject;
		}

		@Override
		public Project project() {
			return project;
		}

		@Override
		public String variantName() {
			return "nopu";
		}

		@Override
		public String displayName() {
			return "sources ':nopu'";
		}
	}

	@Nested
	class CompileTaskTest implements CCompileTester {
		@BeforeEach
		void configureTargetPlatform() {
			subject().getTargetPlatform().set(NativePlatformFactory.create(TargetMachines.of("macos-x64")));
		}

		public CCompileTask subject() {
			return (CCompileTask) project.getTasks().getByName("compileNopu");
		}

		@Test
		void hasDestinationDirectoryUnderObjsInsideBuildDirectory() {
			assertThat(subject().getDestinationDirectory(),
				providerOf(aFile(withAbsolutePath(containsString("/build/objs/")))));
		}

		@Test
		void includesLanguageSourceSetNameInDestinationDirectory() {
			assertThat(subject().getDestinationDirectory(), providerOf(aFileNamed("nopu")));
		}
	}
}
