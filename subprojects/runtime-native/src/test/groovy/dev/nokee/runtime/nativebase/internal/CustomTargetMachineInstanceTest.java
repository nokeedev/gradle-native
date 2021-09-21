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
package dev.nokee.runtime.nativebase.internal;

import com.google.common.testing.EqualsTester;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.TargetMachineTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class CustomTargetMachineInstanceTest {
	@Nested
	class WindowsX64Test implements TargetMachineTester {
		@Override
		public TargetMachine subject() {
			return TargetMachines.of("windows-x64");
		}

		@Test
		public void hasOperatingSystemFamily() {
			assertThat(subject().getOperatingSystemFamily(), named("windows"));
		}

		@Test
		public void hasMachineArchitecture() {
			assertThat(subject().getArchitecture(), named("x64"));
		}

		@Test
		void checkToString() {
			assertThat(subject(), hasToString("windowsX64"));
		}
	}

	@Nested
	class MunixAach64Test implements TargetMachineTester {
		@Override
		public TargetMachine subject() {
			return TargetMachines.of("munix-aarch64");
		}

		@Test
		public void hasOperatingSystemFamily() {
			assertThat(subject().getOperatingSystemFamily(), named("munix"));
		}

		@Test
		public void hasMachineArchitecture() {
			assertThat(subject().getArchitecture(), named("aarch64"));
		}

		@Test
		void checkToString() {
			assertThat(subject(), hasToString("munixAarch64"));
		}
	}

	@Nested
	class NamedWindowsX86Test implements TargetMachineTester {
		@Override
		public TargetMachine subject() {
			return TargetMachines.of("windows-x86").named("foo");
		}

		@Test
		public void hasOperatingSystemFamily() {
			assertThat(subject().getOperatingSystemFamily(), named("windows"));
		}

		@Test
		public void hasMachineArchitecture() {
			assertThat(subject().getArchitecture(), named("x86"));
		}

		@Test
		void checkToString() {
			assertThat(subject(), hasToString("foo"));
		}
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(TargetMachines.of("windows-x64"), TargetMachines.of("windows-x64"))
			.addEqualityGroup(TargetMachines.of("linux-x86"))
			.addEqualityGroup(TargetMachines.of("macos-x64"))
			.addEqualityGroup(TargetMachines.of("macos-x64").named("foo"))
			.testEquals();
	}
}
