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
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.objectivecpp.tasks.ObjectiveCppCompile;
import dev.nokee.platform.jni.internal.JniLibraryComponentInternal;
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
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentIntegrationTest.realize;
import static dev.nokee.utils.FileCollectionUtils.sourceDirectories;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
class JavaNativeInterfaceLibraryVariantObjectiveCppLanguagePluginIntegrationTest extends AbstractPluginTest {
	private JniLibrary subject;

	@BeforeEach
	void createSubject() {
		subject = components(project).register("xapi", JniLibraryComponentInternal.class)
			.map(it -> {
				it.getTargetMachines().set(ImmutableSet.of(it.getMachines().getLinux().architecture("x64"), it.getMachines().getWindows().getX86()));
				return it.getVariants().get().iterator().next();
			})
			.get();
	}

	@Test
	void hasObjectiveCppSourceSetWhenObjectiveCppLanguagePluginApplied() {
		assertThat(subject.getSources().get(), hasItem(allOf(named("xapiLinuxX64ObjectiveCpp"), isA(ObjectiveCppSourceSet.class))));
	}

	@Nested
	class ObjectsTaskTest {
		public Task subject() {
			return project.getTasks().getByName("objectsXapiLinuxX64");
		}

		@Test
		void dependsOnObjectiveCppCompileTask() {
			assertThat(subject(), dependsOn(hasItem(allOf(named("compileXapiLinuxX64ObjectiveCpp"), isA(ObjectiveCppCompile.class)))));
		}
	}

	@Nested
	class NativeCompileOnlyConfigurationTest implements NativeCompileOnlyConfigurationTester {
		public Configuration subject() {
			return project.getConfigurations().getByName("xapiLinuxX64NativeCompileOnly");
		}

		@Override
		public String displayName() {
			return "variant ':xapi:linuxX64'";
		}
	}

	@Nested
	class HeaderSearchPathsConfigurationTest implements NativeHeaderSearchPathsConfigurationTester {
		public Configuration subject() {
			return realize(project.getConfigurations().getByName("xapiLinuxX64ObjectiveCppHeaderSearchPaths"));
		}

		@Override
		public String displayName() {
			return "Objective-C++ sources ':xapi:linuxX64:sharedLibrary:objectiveCpp'";
		}

		@Override
		public String implementationConfigurationName() {
			return "xapiLinuxX64NativeCompileOnly";
		}

		@Override
		public String compileOnlyConfigurationName() {
			return "xapiLinuxX64NativeImplementation";
		}
	}

	@Nested
	class CompileTaskTest {
		public ObjectiveCppCompileTask subject() {
			return (ObjectiveCppCompileTask) project.getTasks().getByName("compileXapiLinuxX64ObjectiveCpp");
		}

		@Test
		void hasTargetPlatform() {
			assertThat(subject().getTargetPlatform(), providerOf(named("linuxx86-64")));
		}
	}

	@Nested
	class ObjectiveCppSourceSetTest {
		public ObjectiveCppSourceSet subject() {
			return (ObjectiveCppSourceSet) subject.getSources().get().stream().filter(it -> it.getName().equals("xapiLinuxX64ObjectiveCpp")).collect(onlyElement());
		}

		@Test
		void usesConventionalSourceLocation() {
			assertThat(sourceDirectories(subject().getSource()), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/src/xapi/objectiveCpp"))))));
		}

		@Test
		void usesLegacyConventionalSourceLocation() {
			assertThat(sourceDirectories(subject().getSource()), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/src/xapi/objcpp"))))));
		}

		@Test
		void usesConventionalHeadersLocation() {
			assertThat(sourceDirectories(subject().getHeaders()), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/src/xapi/headers"))))));
		}
	}
}
