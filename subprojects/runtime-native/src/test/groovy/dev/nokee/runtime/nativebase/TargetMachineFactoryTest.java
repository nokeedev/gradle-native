package dev.nokee.runtime.nativebase;

import com.google.common.collect.Sets;
import dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.MachineArchitectureConfigurer;
import dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.OperatingSystemFamilyConfigurer;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.ImmutableSet.of;
import static dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.MachineArchitectureConfigurer.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertAll;

class TargetMachineFactoryTest implements TargetMachineFactoryTester{
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
	class AdhocOperatingSystemFamilyTest implements KnownOperatingSystemFamilyTester, UnknownOperatingSystemFamilyTester {
		@Override
		public OperatingSystemFamily createSubject(String name) {
			return TargetMachineFactoryTest.this.createSubject().os(name).getOperatingSystemFamily();
		}
	}
}
