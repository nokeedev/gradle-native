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
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.jni.internal.JarTaskRegistrationActionFactory;
import dev.nokee.platform.jni.internal.JniJarBinaryRegistrationFactory;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.Jar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.TaskMatchers.description;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
public class JniJarBinaryIntegrationTest extends AbstractPluginTest {
	private JniJarBinary subject;

	@BeforeEach
	void createSubject() {
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val componentIdentifier = ComponentIdentifier.of("poto", ProjectIdentifier.of(project));
		registry.register(ModelRegistration.builder().withComponent(componentIdentifier).withComponent(toPath(componentIdentifier)).build());
		val variantIdentifier = VariantIdentifier.of("qile", Variant.class, componentIdentifier);
		registry.register(ModelRegistration.builder().withComponent(variantIdentifier).withComponent(toPath(variantIdentifier)).build());
		subject = registry.register(new JniJarBinaryRegistrationFactory(new JarTaskRegistrationActionFactory(() -> project.getExtensions().getByType(TaskRegistrationFactory.class), () -> project.getExtensions().getByType(ModelRegistry.class), () -> project.getExtensions().getByType(ModelPropertyRegistrationFactory.class))).create(BinaryIdentifier.of(variantIdentifier, BinaryIdentity.of("tuva", "Liha binary")))).as(JniJarBinary.class).get();
	}

	@Nested
	class BinaryTest extends JniJarBinaryIntegrationTester {
		@Override
		public JniJarBinary subject() {
			return subject;
		}

		@Override
		public String variantName() {
			return "potoQileTuva";
		}

		@Override
		public Project project() {
			return project;
		}

		@Nested
		class JniJarTaskTest {
			public Jar subject() {
				return (Jar) project().getTasks().getByName("jar" + capitalize(variantName()));
			}

			@Test
			void hasDescription() {
				assertThat(subject(), description("Assembles a JAR archive containing the shared library for Liha binary ':poto:qile:tuva'."));
			}
		}
	}
}
