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

import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
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
import static dev.nokee.utils.FileCollectionUtils.sourceDirectories;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
@PluginRequirement.Require(id = "dev.nokee.cpp-language")
class JavaNativeInterfaceLibraryVariantCppLanguagePluginIntegrationTest extends AbstractPluginTest {
	private JniLibrary subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryComponentRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val componentIdentifier = ComponentIdentifier.of("difi", ProjectIdentifier.of(project));
		subject = registry.register(factory.create(componentIdentifier))
			.as(JavaNativeInterfaceLibrary.class)
			.configure(it -> it.getTargetMachines().set(ImmutableSet.of(it.getMachines().getWindows().getX86(), it.getMachines().getLinux().getX86_64())))
			.map(it -> it.getVariants().get().iterator().next())
			.get();
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
			assertThat(sourceDirectories(subject().getSource()), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/src/difi/cpp"))))));
		}

		@Test
		void usesConventionalHeadersLocation() {
			assertThat(sourceDirectories(subject().getHeaders()), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/src/difi/headers"))))));
		}
	}
}
