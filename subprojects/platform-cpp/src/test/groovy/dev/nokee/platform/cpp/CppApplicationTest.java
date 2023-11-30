/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.cpp;

import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.cpp.HasCppSourcesTester;
import dev.nokee.language.cpp.internal.plugins.CppLanguageBasePlugin;
import dev.nokee.language.nativebase.HasPrivateHeadersTester;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.testers.BinaryAwareComponentTester;
import dev.nokee.platform.base.testers.ComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import dev.nokee.platform.base.testers.HasDevelopmentVariantTester;
import dev.nokee.platform.base.testers.TaskAwareComponentTester;
import dev.nokee.platform.base.testers.VariantAwareComponentTester;
import dev.nokee.platform.base.testers.VariantDimensionsIntegrationTester;
import dev.nokee.platform.cpp.internal.CppApplicationSpec;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.testers.TargetBuildTypeAwareComponentTester;
import dev.nokee.platform.nativebase.testers.TargetMachineAwareComponentTester;
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
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class CppApplicationTest implements ComponentTester<CppApplication>
	, HasBaseNameTester
	, DependencyAwareComponentTester<NativeApplicationComponentDependencies>
	, VariantAwareComponentTester<View<NativeApplication>>
	, BinaryAwareComponentTester<View<Binary>>
	, TaskAwareComponentTester<View<Task>>
	, HasCppSourcesTester
	, HasPrivateHeadersTester
	, HasDevelopmentVariantTester
	, TargetMachineAwareComponentTester
	, TargetBuildTypeAwareComponentTester
{
	private CppApplication subject;
	@Getter @TempDir File testDirectory;

	@BeforeEach
	void createASubject() {
		subject = createSubject("sari");
	}

	public CppApplication createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CppLanguageBasePlugin.class);
		val component = components(project).register(componentName, CppApplicationSpec.class).get();
		return component;
	}

	@Override
	public CppApplication subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("sari"));
	}

	@Nested
	class ComponentTasksTest {
		public View<Task> subject() {
			return subject.getTasks();
		}

		@Test
		void hasAssembleTask() {
			assertThat(subject().get(), hasItem(named("assembleSari")));
		}
	}

	@Nested
	class AssembleTaskTest {
		public Task subject() {
			return subject.getTasks().filter(it -> it.getName().equals("assembleSari")).get().get(0);
		}

		@Test
		public void hasBuildGroup() {
			assertThat(subject(), TaskMatchers.group("build"));
		}

		@Test
		public void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the outputs of the C++ application ':sari'."));
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
