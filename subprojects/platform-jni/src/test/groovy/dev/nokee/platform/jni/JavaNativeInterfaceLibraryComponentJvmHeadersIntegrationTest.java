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
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
class JavaNativeInterfaceLibraryComponentJvmHeadersIntegrationTest extends AbstractPluginTest {
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		val identifier = ComponentIdentifier.of("sine", ProjectIdentifier.ofRootProject());
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryComponentRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		this.subject = registry.register(factory.create(identifier)).as(JavaNativeInterfaceLibrary.class).get();
	}

	abstract class JvmHeadersTester {
		public NativeSourceCompile subject() {
			return (NativeSourceCompile) project.getTasks().getByName("compileSine" + getClass().getSimpleName().replace("When", "").replace("LanguagePluginAppliedTest", ""));
		}

		@Test
		void hasJvmHeadersOnLinux() {
			subject.getTargetMachines().set(ImmutableSet.of(of("linux-x86")));
			subject.getTasks().get(); // force realize
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/include"))))));
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/include/linux"))))));
		}

		@Test
		void hasJvmHeadersOnWindows() {
			subject.getTargetMachines().set(ImmutableSet.of(of("windows-x86")));
			subject.getTasks().get(); // force realize
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/include"))))));
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/include/win32"))))));
		}

		@Test
		void hasJvmHeadersOnMacOS() {
			subject.getTargetMachines().set(ImmutableSet.of(of("macos-x64")));
			subject.getTasks().get(); // force realize
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/include"))))));
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/include/darwin"))))));
		}

		@Test
		void hasJvmHeadersOnFreeBSD() {
			subject.getTargetMachines().set(ImmutableSet.of(of("freebsd-x86")));
			subject.getTasks().get(); // force realize
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/include"))))));
			assertThat(subject().getHeaderSearchPaths(), providerOf(hasItem(aFile(withAbsolutePath(endsWith("/include/freebsd"))))));
		}
	}

	@Nested
	@PluginRequirement.Require(id = "dev.nokee.c-language")
	class WhenCLanguagePluginAppliedTest extends JvmHeadersTester {}

	@Nested
	@PluginRequirement.Require(id = "dev.nokee.cpp-language")
	class WhenCppLanguagePluginAppliedTest extends JvmHeadersTester {}

	@Nested
	@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
	class WhenObjectiveCLanguagePluginAppliedTest extends JvmHeadersTester {}

	@Nested
	@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
	class WhenObjectiveCppLanguagePluginAppliedTest extends JvmHeadersTester {}
}
