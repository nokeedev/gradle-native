package dev.nokee.runtime.nativebase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

/** @see KnownMachineArchitectureTester */
interface KnownMachineArchitectureIntel32BitTester {
	MachineArchitecture createSubject(String name);

	@ParameterizedTest(name = "has 32-bit pointer size [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.MachineArchitectureTestUtils#commonIntel32BitNames")
	default void intel32BitArchitectureHas32BitPointerSize(String name) {
		assertAll(
			() -> assertThat(createSubject(name).is32Bit(), is(true)),
			() -> assertThat(createSubject(name).is64Bit(), is(false))
		);
	}

	@ParameterizedTest(name = "has canonical name [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.MachineArchitectureTestUtils#commonIntel32BitNames")
	default void intel32BitArchitectureHasCanonicalName(String name) {
		assertThat(createSubject(name).getCanonicalName(), equalTo("x86"));
	}
}
