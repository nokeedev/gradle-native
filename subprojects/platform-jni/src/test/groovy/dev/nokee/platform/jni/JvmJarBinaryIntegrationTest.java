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
package dev.nokee.platform.jni;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.BinaryIdentity;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.jni.internal.JvmJarBinaryRegistrationFactory;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.parentFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.TaskMatchers.description;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
class JvmJarBinaryIntegrationTest extends AbstractPluginTest {
	private JvmJarBinary subject;

	@BeforeEach
	void createSubject() {
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val componentIdentifier = ComponentIdentifier.of("rina", ProjectIdentifier.of(project));
		registry.register(ModelRegistration.builder().withComponent(componentIdentifier).build());
		val factory = project.getExtensions().getByType(JvmJarBinaryRegistrationFactory.class);
		subject = registry.register(factory.create(BinaryIdentifier.of(componentIdentifier, BinaryIdentity.of("wuke", "FASI binary"))).build()).as(JvmJarBinary.class).get();
	}

	@Nested
	class BinaryTest extends JvmJarBinaryIntegrationTester {
		@Override
		public JvmJarBinary subject() {
			return subject;
		}

		@Override
		public String variantName() {
			return "rinaWuke";
		}

		@Override
		public Project project() {
			return project;
		}

		@Nested
		class JvmJarTaskTest {
			public Jar subject() {
				return (Jar) project().getTasks().getByName("jar" + capitalize(variantName()));
			}

			@Test
			void hasDescription() {
				assertThat(subject(), description("Assembles a JAR archive containing the classes for FASI binary ':rina:wuke'."));
			}

			@Test
			void usesBinaryNameForJarTaskArchiveBaseNameConvention() {
				subject().getArchiveBaseName().set((String) null);
				assertThat(subject().getArchiveBaseName(), providerOf("wuke"));
			}

			@Test
			void hasDestinationDirectoryUnderLibsInsideBuildDirectory() {
				subject().getDestinationDirectory().set((File) null);
				assertThat(subject().getDestinationDirectory(),
					providerOf(aFile(withAbsolutePath(endsWith("/build/libs")))));
			}

			@Test
			void usesDestinationDirectoryAsArchiveFileParentDirectory() {
				val newDestinationDirectory = project().file("some-new-destination-directory");
				subject().getDestinationDirectory().set(newDestinationDirectory);
				assertThat(subject().getArchiveFile(), providerOf(aFile(parentFile(is(newDestinationDirectory)))));
			}
		}
	}
}
