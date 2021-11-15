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

import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.testers.*;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeLibraryPlugin.nativeLibrary;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class NativeLibraryComponentTest implements ComponentTester<NativeLibraryExtension>
	, HasBaseNameTester
	, DependencyAwareComponentTester<NativeLibraryComponentDependencies>
	, VariantAwareComponentTester<VariantView<NativeLibrary>>
	, BinaryAwareComponentTester<BinaryView<Binary>>
	, TaskAwareComponentTester<TaskView<Task>>
{
	private NativeLibraryExtension subject;
	@Getter @TempDir File testDirectory;

	@BeforeEach
	void createASubject() {
		subject = createSubject("zasi");
	}

	public NativeLibraryExtension createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(testDirectory);
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		val component = project.getExtensions().getByType(ModelRegistry.class).register(nativeLibrary(componentName, project)).as(NativeLibraryExtension.class).get();
		((FunctionalSourceSet) component.getSources()).get(); // force realize
		return component;
	}

	@Override
	public NativeLibraryExtension subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("zasi"));
	}

	@Nested
	class ComponentTasksTest {
		public TaskView<Task> subject() {
			return subject.getTasks();
		}

		@Test
		void hasAssembleTask() {
			assertThat(subject().get(), hasItem(named("assembleZasi")));
		}
	}

	@Nested
	class AssembleTaskTest {
		public Task subject() {
			return subject.getTasks().filter(it -> it.getName().equals("assembleZasi")).get().get(0);
		}

		@Test
		public void hasBuildGroup() {
			assertThat(subject(), TaskMatchers.group("build"));
		}

		@Test
		public void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the outputs of the native library ':zasi'."));
		}
	}

	@Nested
	class VariantDimensionsTest extends VariantDimensionsIntegrationTester {
		@Override
		public VariantAwareComponent<?> subject() {
			return subject;
		}
	}
}
