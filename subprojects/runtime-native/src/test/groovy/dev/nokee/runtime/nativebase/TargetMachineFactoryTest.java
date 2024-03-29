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
package dev.nokee.runtime.nativebase;

import com.google.common.collect.Sets;
import dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.MachineArchitectureConfigurer;
import dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.OperatingSystemFamilyConfigurer;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.of;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.MachineArchitectureConfigurer.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertAll;

class TargetMachineFactoryTest implements TargetMachineFactoryTester {
	@Override
	public TargetMachineFactory createSubject() {
		return NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY;
	}

	@ParameterizedTest(name = "provides attributes for resolvable configuration {arguments}")
	@MethodSource("toStringConfigurer")
	void checkToStrings(OperatingSystemFamilyConfigurer os, MachineArchitectureConfigurer arch) {
		val factory = createSubject();
		assertAll(
			() -> assertThat(os.apply(factory), hasToString(os.getName() + ":host")),
			() -> assertThat(arch.apply(os.apply(factory)), hasToString(os.getName() + ":" + arch.getName()))
		);
	}

	static Stream<Arguments> toStringConfigurer() {
		return Sets.cartesianProduct(copyOf(OperatingSystemFamilyConfigurer.values()), of(X86, X86_64)).stream().map(it -> Arguments.of(it.get(0), it.get(1)));
	}

	@Nested
	class AdhocMachineArchitectureTest implements MachineArchitectureTester {
		@Override
		public MachineArchitecture createSubject(String name) {
			return TargetMachineFactoryTest.this.createSubject().os("some-os").architecture(name).getArchitecture();
		}
	}

	@Nested
	class AdhocOperatingSystemFamilyTest implements OperatingSystemFamilyTester {
		@Override
		public OperatingSystemFamily createSubject(String name) {
			return TargetMachineFactoryTest.this.createSubject().os(name).getOperatingSystemFamily();
		}
	}

	@Test
	void checkKnownOperatingSystemAndHostArchitectureTargetMachineName() {
		assertAll(
			() -> assertThat(createSubject().getFreeBSD(), named(OperatingSystemFamily.FREE_BSD)),
			() -> assertThat(createSubject().getLinux(), named(OperatingSystemFamily.LINUX)),
			() -> assertThat(createSubject().getMacOS(), named(OperatingSystemFamily.MACOS)),
			() -> assertThat(createSubject().getWindows(), named(OperatingSystemFamily.WINDOWS))
		);
	}

	@Test
	void checkAdhocOperatingSystemAndHostArchitectureTargetMachineName() {
		assertAll(
			() -> assertThat(createSubject().os("foo"), named("foo")),
			() -> assertThat(createSubject().os("bar"), named("bar")),
			() -> assertThat(createSubject().os("foo-bar"), named("foo-bar"))
		);
	}

	@Test
	void checkTargetMachineName() {
		assertAll(
			() -> assertThat(createSubject().getWindows().getX86(), named("windowsX86")),
			() -> assertThat(createSubject().getMacOS().getX86_64(), named("macosX86-64")),
			() -> assertThat(createSubject().getLinux().architecture("foo"), named("linuxFoo"))
		);
	}
}
