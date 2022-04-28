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
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryVariantRegistrationFactory;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.TaskMatchers.dependsOn;
import static dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentIntegrationTest.realize;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
@PluginRequirement.Require(id = "dev.nokee.cpp-language")
class JavaNativeInterfaceLibraryVariantCppLanguagePluginIntegrationTest extends AbstractPluginTest {
	private JniLibrary subject;

	@BeforeEach
	void createSubject() {
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val componentIdentifier = ComponentIdentifier.of("difi", ProjectIdentifier.of(project));
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(componentIdentifier)).withComponent(new FullyQualifiedNameComponent("difi")).build());
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryVariantRegistrationFactory.class);
		val variantIdentifier = VariantIdentifier.of(DefaultBuildVariant.of(of("windows-x86")), componentIdentifier);
		subject = registry.register(factory.create(variantIdentifier)).as(JniLibrary.class).get();
	}

	@Test
	void hasCppSourceSetWhenCppLanguagePluginApplied() {
		assertThat(subject.getSources().get(), hasItem(allOf(named("difiWindowsX86Cpp"), isA(CppSourceSet.class))));
	}

	@Nested
	class ObjectsTaskTest {
		public Task subject() {
			return project.getTasks().getByName("objectsDifiWindowsX86");
		}

		@Test
		void dependsOnCppCompileTask() {
			assertThat(subject(), dependsOn(hasItem(allOf(named("compileDifiWindowsX86Cpp"), isA(CppCompile.class)))));
		}
	}

	@Nested
	class NativeCompileOnlyConfigurationTest implements NativeCompileOnlyConfigurationTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("difiWindowsX86NativeCompileOnly");
		}

		@Override
		public String displayName() {
			return "variant ':difi:windowsX86'";
		}
	}

	@Nested
	class HeaderSearchPathsConfigurationTest implements NativeHeaderSearchPathsConfigurationTester {
		public Configuration subject() {
			return realize(project.getConfigurations().getByName("difiWindowsX86CppHeaderSearchPaths"));
		}

		@Override
		public String displayName() {
			return "C++ sources ':difi:windowsX86:sharedLibrary:cpp'";
		}

		@Override
		public String implementationConfigurationName() {
			return "difiWindowsX86NativeImplementation";
		}

		@Override
		public String compileOnlyConfigurationName() {
			return "difiWindowsX86NativeCompileOnly";
		}
	}

	@Nested
	class CompileTaskTest {
		public CppCompileTask subject() {
			return (CppCompileTask) project.getTasks().getByName("compileDifiWindowsX86Cpp");
		}

		@Test
		void hasTargetPlatform() {
			assertThat(subject().getTargetPlatform(), providerOf(named("windowsx86")));
		}
	}

	@Nested
	class CppSourceSetTest {
		public CppSourceSet subject() {
			return (CppSourceSet) subject.getSources().get().stream().filter(it -> it.getName().equals("difiWindowsX86Cpp")).collect(onlyElement());
		}

		@Test
		void usesConventionalSourceLocation() {
			assertThat(subject().getSourceDirectories(), hasItem(aFile(withAbsolutePath(endsWith("/src/difi/cpp")))));
		}

		@Test
		void usesConventionalHeadersLocation() {
			assertThat(((HasHeaders) subject()).getHeaders().getSourceDirectories(), hasItem(aFile(withAbsolutePath(endsWith("/src/difi/headers")))));
		}
	}
}
