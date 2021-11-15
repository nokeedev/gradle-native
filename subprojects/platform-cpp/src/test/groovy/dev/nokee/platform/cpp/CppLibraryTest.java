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
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.HasCppSourcesTester;
import dev.nokee.language.cpp.internal.plugins.CppLanguageBasePlugin;
import dev.nokee.language.nativebase.HasPrivateHeadersTester;
import dev.nokee.language.nativebase.HasPublicHeadersTester;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.testers.*;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.testers.TargetBuildTypeAwareComponentTester;
import dev.nokee.platform.nativebase.testers.TargetLinkageAwareComponentTester;
import dev.nokee.platform.nativebase.testers.TargetMachineAwareComponentTester;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.cpp.internal.plugins.CppLibraryPlugin.cppLibrary;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class CppLibraryTest implements ComponentTester<CppLibrary>
	, SourceAwareComponentTester<CppLibrary>
	, HasBaseNameTester
	, DependencyAwareComponentTester<NativeLibraryComponentDependencies>
	, VariantAwareComponentTester<VariantView<NativeLibrary>>
	, BinaryAwareComponentTester<BinaryView<Binary>>
	, TaskAwareComponentTester<TaskView<Task>>
	, HasCppSourcesTester
	, HasPrivateHeadersTester
	, HasPublicHeadersTester
	, HasDevelopmentVariantTester
	, TargetMachineAwareComponentTester
	, TargetBuildTypeAwareComponentTester
	, TargetLinkageAwareComponentTester
{
	private CppLibrary subject;
	@Getter @TempDir File testDirectory;

	@BeforeEach
	void createASubject() {
		subject = createSubject("gori");
	}

	@Override
	public CppLibrary createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(CppLanguageBasePlugin.class);
		val component = project.getExtensions().getByType(ModelRegistry.class).register(cppLibrary(componentName, project)).as(CppLibrary.class).get();
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(
			new SourcesUnderTest("cpp", CppSourceSet.class, "cppSources"),
			new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"),
			new SourcesUnderTest("public", NativeHeaderSet.class, "publicHeaders"));
	}

	@Override
	public CppLibrary subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("gori"));
	}

	@Nested
	class ComponentTasksTest {
		public TaskView<Task> subject() {
			return subject.getTasks();
		}

		@Test
		void hasAssembleTask() {
			assertThat(subject().get(), hasItem(named("assembleGori")));
		}
	}

	@Nested
	class AssembleTaskTest {
		public Task subject() {
			return subject.getTasks().filter(it -> it.getName().equals("assembleGori")).get().get(0);
		}

		@Test
		public void hasBuildGroup() {
			assertThat(subject(), TaskMatchers.group("build"));
		}

		@Test
		public void hasDescription() {
			assertThat(subject(), TaskMatchers.description("Assembles the outputs of the C++ library ':gori'."));
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
