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
package dev.nokee.platform.nativebase;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.platform.nativebase.testers.SharedLibraryBinaryIntegrationTester;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
class SharedLibraryBinaryTest extends AbstractPluginTest {
	private SharedLibraryBinary subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(SharedLibraryBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		val componentIdentifier = ComponentIdentifier.of("nuli", projectIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(componentIdentifier)).build());
		val variantIdentifier = VariantIdentifier.of("cuzu", Variant.class, componentIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(variantIdentifier)).build());
		subject = registry.register(factory.create(BinaryIdentifier.of(variantIdentifier, "ruca"))).as(SharedLibraryBinary.class).get();
	}

	@Nested
	class BinaryTest extends SharedLibraryBinaryIntegrationTester {
		@BeforeEach
		public void configureTargetPlatform() {
			((LinkSharedLibraryTask) project.getTasks().getByName("link" + capitalize(variantName()))).getTargetPlatform()
				.set(create(of("macos-x64")));
		}

		@Override
		public SharedLibraryBinary subject() {
			return subject;
		}

		@Override
		public Project project() {
			return project;
		}

		@Override
		public String variantName() {
			return "nuliCuzuRuca";
		}

		@Override
		public String displayName() {
			return "binary ':nuli:cuzu:ruca'";
		}

		@Test
		void usesBinaryNameAsBaseNameByDefault() {
			subject().getBaseName().set((String) null); // force convention
			assertThat(subject().getBaseName(), providerOf("ruca"));
		}
	}
}
