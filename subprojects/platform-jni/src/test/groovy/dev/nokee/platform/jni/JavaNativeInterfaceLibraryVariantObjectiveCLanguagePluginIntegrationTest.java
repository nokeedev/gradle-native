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
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
class JavaNativeInterfaceLibraryVariantObjectiveCLanguagePluginIntegrationTest extends AbstractPluginTest {
	private JniLibrary subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryComponentRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val componentIdentifier = ComponentIdentifier.of("veda", ProjectIdentifier.of(project));
		subject = registry.register(factory.create(componentIdentifier))
			.as(JavaNativeInterfaceLibrary.class)
			.configure(it -> it.getTargetMachines().set(ImmutableSet.of(it.getMachines().getWindows().architecture("x64"), it.getMachines().getLinux().getX86())))
			.map(it -> it.getVariants().get().iterator().next())
			.get();
	}

	@Test
	void hasObjectiveCSourceSetWhenObjectiveCLanguagePluginApplied() {
		assertThat(subject.getSources().get(), hasItem(allOf(named("vedaWindowsX64ObjectiveC"), isA(ObjectiveCSourceSet.class))));
	}

	@Nested
	class ObjectsTaskTest {
		public Task subject() {
			return project.getTasks().getByName("objectsVedaWindowsX64");
		}

		@Test
		void dependsOnObjectiveCCompileTask() {
			assertThat(subject(), dependsOn(hasItem(allOf(named("compileVedaWindowsX64ObjectiveC"), isA(ObjectiveCCompile.class)))));
		}
	}

	@Nested
	class NativeCompileOnlyConfigurationTest implements NativeCompileOnlyConfigurationTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("vedaWindowsX64NativeCompileOnly");
		}

		@Override
		public String displayName() {
			return "variant ':veda:windowsX64'";
		}
	}

	@Nested
	class HeaderSearchPathsConfigurationTest implements NativeHeaderSearchPathsConfigurationTester {
		public Configuration subject() {
			return realize(project.getConfigurations().getByName("vedaWindowsX64ObjectiveCHeaderSearchPaths"));
		}

		@Override
		public String displayName() {
			return "Objective-C sources ':veda:windowsX64:sharedLibrary:objectiveC'";
		}

		@Override
		public String implementationConfigurationName() {
			return "vedaWindowsX64NativeImplementation";
		}

		@Override
		public String compileOnlyConfigurationName() {
			return "vedaWindowsX64NativeCompileOnly";
		}
	}

	@Nested
	class CompileTaskTest {
		public ObjectiveCCompileTask subject() {
			return (ObjectiveCCompileTask) project.getTasks().getByName("compileVedaWindowsX64ObjectiveC");
		}

		@Test
		void hasTargetPlatform() {
			assertThat(subject().getTargetPlatform(), providerOf(named("windowsx86-64")));
		}
	}

	@Nested
	class ObjectiveCSourceSetTest {
		public ObjectiveCSourceSet subject() {
			return (ObjectiveCSourceSet) subject.getSources().get().stream().filter(it -> it.getName().equals("vedaWindowsX64ObjectiveC")).collect(onlyElement());
		}

		@Test
		void usesConventionalSourceLocation() {
			assertThat(subject().getSourceDirectories(), hasItem(aFile(withAbsolutePath(endsWith("/src/veda/objectiveC")))));
		}

		@Test
		void usesLegacyConventionalSourceLocation() {
			assertThat(subject().getSourceDirectories(), hasItem(aFile(withAbsolutePath(endsWith("/src/veda/objc")))));
		}

		@Test
		void usesConventionalHeadersLocation() {
			assertThat(((HasHeaders) subject()).getHeaders().getSourceDirectories(), hasItem(aFile(withAbsolutePath(endsWith("/src/veda/headers")))));
		}
	}
}
