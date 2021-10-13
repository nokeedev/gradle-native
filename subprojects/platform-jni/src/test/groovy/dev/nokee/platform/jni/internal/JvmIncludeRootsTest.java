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
package dev.nokee.platform.jni.internal;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.platform.nativebase.internal.NativePlatformFactory;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import org.gradle.api.Project;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.platform.jni.internal.plugins.JvmIncludeRoots.jvmIncludes;
import static dev.nokee.platform.jni.internal.plugins.NativeCompileTaskProperties.targetPlatform;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;

class JvmIncludeRootsTest {
	private final Project project = ProjectTestUtils.rootProject();

	private AbstractNativeCompileTask createCompileTask(TargetMachine targetMachine) {
		// NOTE: we don't really care about the task type, as long as it contains a target platform property
		return project.getTasks().create("test", CCompileTask.class, targetPlatform(set(NativePlatformFactory.create(targetMachine))));
	}

	@Nested
	class WindowsOperatingSystemTest {
		private final AbstractNativeCompileTask subject = createCompileTask(TargetMachines.of("windows-x86"));

		@Test
		void returnsJvmIncludesForTargetPlatform() {
			assertThat(jvmIncludes().apply(subject), providerOf(contains(
				aFile(withAbsolutePath(endsWith("/include"))),
				aFile(withAbsolutePath(endsWith("/include/win32")))
			)));
		}
	}

	@Nested
	class LinuxOperatingSystemTest {
		private final AbstractNativeCompileTask subject = createCompileTask(TargetMachines.of("linux-x86"));

		@Test
		void returnsJvmIncludesForTargetPlatform() {
			assertThat(jvmIncludes().apply(subject), providerOf(contains(
				aFile(withAbsolutePath(endsWith("/include"))),
				aFile(withAbsolutePath(endsWith("/include/linux")))
			)));
		}
	}

	@Nested
	class MacOsOperatingSystemTest {
		private final AbstractNativeCompileTask subject = createCompileTask(TargetMachines.of("macos-x86"));

		@Test
		void returnsJvmIncludesForTargetPlatform() {
			assertThat(jvmIncludes().apply(subject), providerOf(contains(
				aFile(withAbsolutePath(endsWith("/include"))),
				aFile(withAbsolutePath(endsWith("/include/darwin")))
			)));
		}
	}

	@Nested
	class FreeBsdOperatingSystemTest {
		private final AbstractNativeCompileTask subject = createCompileTask(TargetMachines.of("freebsd-x86"));

		@Test
		void returnsJvmIncludesForTargetPlatform() {
			assertThat(jvmIncludes().apply(subject), providerOf(contains(
				aFile(withAbsolutePath(endsWith("/include"))),
				aFile(withAbsolutePath(endsWith("/include/freebsd")))
			)));
		}
	}

	@Nested
	class UnknownOperatingSystemTest {
		private final AbstractNativeCompileTask subject = createCompileTask(TargetMachines.of("unknown-x86"));

		@Test
		void returnsJvmIncludesForTargetPlatform() {
			assertThat(jvmIncludes().apply(subject), providerOf(contains(
				aFile(withAbsolutePath(endsWith("/include")))
			)));
		}
	}
}
