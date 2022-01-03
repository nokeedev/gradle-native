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
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.testers.HasConfigurableSourceTester;
import dev.nokee.language.base.testers.LanguageSourceSetIntegrationTester;
import dev.nokee.language.base.testers.LanguageSourceSetTester;
import dev.nokee.language.nativebase.NativeCompileTaskObjectFilesTester;
import dev.nokee.language.nativebase.NativeCompileTaskTester;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetRegistrationFactory;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetSpec;
import dev.nokee.language.swift.tasks.SwiftCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createDependency;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static dev.nokee.utils.ConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.swift-language-base")
@PluginRequirement.Require(type = SwiftCompilerPlugin.class)
class SwiftSourceSetIntegrationTest extends AbstractPluginTest implements LanguageSourceSetTester, HasConfigurableSourceTester {
	private SwiftSourceSetSpec subject;

	@BeforeEach
	void createSubject() {
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(SwiftSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "riku"))).as(SwiftSourceSetSpec.class).get();
	}

	@Override
	public SwiftSourceSetSpec subject() {
		return subject;
	}

	@Test
	void hasToString() {
		assertThat(subject, Matchers.hasToString("Swift sources 'riku'"));
	}

	@Test
	void hasSwiftCompileTask() {
		assertThat(subject.getCompileTask(), providerOf(isA(SwiftCompile.class)));
	}

	@Nested
	class SourceSetTest extends LanguageSourceSetIntegrationTester<SwiftSourceSet> {
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

		public String name() {
			return "riku";
		}

		private Configuration importModules() {
			return project().getConfigurations().getByName(variantName() + "ImportModules");
		}

		@Nested
		class SwiftCompileTaskTest implements NativeCompileTaskTester, NativeCompileTaskObjectFilesTester<SwiftCompileTask> {
			public SwiftCompileTask subject() {
				return (SwiftCompileTask) project().getTasks().getByName("compile" + StringUtils.capitalize(variantName()));
			}

			@Override
			public String languageSourceSetName() {
				return name();
			}

			@Test
			void disablesDebuggableByDefault() {
				assertThat(subject().getDebuggable().value((Boolean) null), providerOf(false));
			}

			@Test
			void disablesOptimizationByDefault() {
				assertThat(subject().getOptimized().value((Boolean) null), providerOf(false));
			}

			@Test
			void linksImportModulesConfigurationToCompileTaskModules() throws IOException {
				val module = Files.createTempDirectory("Foo.swiftmodule").toFile();
				importModules().getDependencies().add(createDependency(objectFactory().fileCollection().from(module)));
				assertThat(subject().getModules(), hasItem(aFile(module)));
			}

			@Test
			void linksHeaderSourcePathsConfigurationToCompileTaskAsFrameworkCompileArguments() throws IOException {
				val artifact = Files.createTempDirectory("Sifo.framework").toFile();
				val frameworkProducer = ProjectTestUtils.createChildProject(project());
				frameworkProducer.getConfigurations().create("apiElements",
					configureAsConsumable()
						.andThen(configureAttributes(forUsage(project().getObjects().named(Usage.class, Usage.SWIFT_API))))
						.andThen(configureAttributes(it -> it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
							project().getObjects().named(LibraryElements.class, "framework-bundle"))))
						.andThen(it -> it.getOutgoing().artifact(artifact, t -> t.setType("framework")))
				);

				importModules().getDependencies().add(createDependency(frameworkProducer));
				assertThat(subject().getModules(), not(hasItem(aFile(artifact))));
				assertThat(subject().getCompilerArgs(), providerOf(containsInRelativeOrder(
					"-F", artifact.getParentFile().getAbsolutePath()
				)));
			}
		}
	}
}
