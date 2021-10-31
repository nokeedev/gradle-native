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
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.testers.ConfigurableSourceSetIntegrationTester;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetRegistrationFactory;
import dev.nokee.language.swift.tasks.SwiftCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static dev.nokee.internal.testing.FileSystemMatchers.*;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.swift-language-base")
class SwiftSourceSetIntegrationTest extends AbstractPluginTest {
	private SwiftSourceSet subject;

	@BeforeEach
	void createSubject() {
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(SwiftSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "riku"), false)).as(SwiftSourceSet.class).get();
	}

	@Nested
	class SourcePropertyTest extends ConfigurableSourceSetIntegrationTester {
		@Override
		public ConfigurableSourceSet subject() {
			return ModelProperties.getProperty(subject, "source").as(ConfigurableSourceSet.class).get();
		}

		@Override
		public File getTemporaryDirectory() throws IOException {
			return Files.createDirectories(project().getProjectDir().toPath()).toFile();
		}
	}

	@Nested
	class CompileTaskPropertyTest {
		public TaskProvider<SwiftCompile> subject() {
			return ModelProperties.getProperty(subject, "compileTask").as(TaskProvider.class).get();
		}

		@Test
		void isCorrectTask() {
			assertThat(subject(), providerOf(named("compileRiku")));
		}
	}

	@Nested
	class ImportModulesConfigurationTest {
		public Configuration subject() {
			return project.getConfigurations().getByName("rikuImportModules");
		}

		@Test
		void isResolvable() {
			assertThat(subject(), ConfigurationMatchers.resolvable());
		}

		@Test
		void hasSwiftApiUsage() {
			assertThat(subject(), ConfigurationMatchers.attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("swift-api"))));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Import modules for sources ':riku'."));
		}
	}

	@Nested
	class CompileTaskTest {
		public SwiftCompileTask subject() {
			return (SwiftCompileTask) project.getTasks().getByName("compileRiku");
		}

		@Test
		void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Compiles the sources ':riku'."));
		}

		@Test
		void defaultsModuleNameToSourceSetName() {
			assertThat(subject().getModuleName(), providerOf("Riku"));
		}

		@Test
		void hasDestinationDirectoryUnderObjsInsideBuildDirectory() {
			assertThat(subject().getDestinationDirectory(),
				providerOf(aFile(withAbsolutePath(containsString("/build/objs/")))));
		}

		@Test
		void includesLanguageSourceSetNameInDestinationDirectory() {
			assertThat(subject().getDestinationDirectory(), providerOf(aFileNamed("riku")));
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

//		@Test
//		public void hasModuleName() {
//			assertThat(subject().getModuleName(), providerOf("Riku"));
//		}
	}
}
