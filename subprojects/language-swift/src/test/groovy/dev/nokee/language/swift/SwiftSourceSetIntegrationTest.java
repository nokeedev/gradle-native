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
package dev.nokee.language.swift;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetRegistrationFactory;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.Project;
import org.gradle.language.swift.SwiftVersion;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.FileSystemMatchers.*;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.swift-language-base")
class SwiftSourceSetIntegrationTest extends AbstractPluginTest {
	private SwiftSourceSet subject;

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(SwiftSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "riku"))).as(SwiftSourceSet.class).get();
	}

	@Test
	void hasToString() {
		assertThat(subject, Matchers.hasToString("Swift sources 'riku'"));
	}

	@Nested
	class SourceSetTest extends SwiftSourceSetIntegrationTester {
		@BeforeEach
		public void configureTargetPlatform() {
			((SwiftCompileTask) project.getTasks().getByName("compileRiku")).getTargetPlatform().set(create(of("macos-x64")));
		}

		@Override
		public SwiftSourceSet subject() {
			return subject;
		}

		@Override
		public Project project() {
			return project;
		}

		@Override
		public String variantName() {
			return "riku";
		}

		@Override
		public String displayName() {
			return "sources ':riku'";
		}

		@Override
		public String name() {
			return "riku";
		}
	}

	@Nested
	class CompileTaskTest {
		@BeforeEach
		void configureTargetPlatform() {
			subject().getTargetPlatform().set(create(of("macos-x64")));
		}

		public SwiftCompileTask subject() {
			return (SwiftCompileTask) project.getTasks().getByName("compileRiku");
		}

		@Test
		void defaultsModuleNameToSourceSetName() {
			assertThat(subject().getModuleName(), providerOf("Riku"));
		}

		@Test
		void defaultsSourceCompatibilityToSwift5() {
			assertThat(subject().getSourceCompatibility(), providerOf(SwiftVersion.SWIFT5));
		}

		@Test
		void hasModuleFileUnderModulesInsideBuildDirectory() {
			assertThat(subject().getModuleFile(),
				providerOf(aFile(withAbsolutePath(containsString("/build/modules/")))));
		}

		@Test
		void includesTargetNameInModuleFile() {
			assertThat(subject().getModuleFile(), providerOf(aFile(parentFile(withAbsolutePath(endsWith("/riku"))))));
		}

		@Test
		void addsMacOsSdkPathToCompilerArguments() {
			subject().getTargetPlatform().set(create(of("macos-x64")));
			assertThat(subject().getCompilerArgs(), providerOf(hasItem("-sdk")));
		}
	}
}
