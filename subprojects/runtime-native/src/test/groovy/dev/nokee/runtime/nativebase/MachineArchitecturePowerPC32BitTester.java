package dev.nokee.runtime.nativebase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

interface MachineArchitecturePowerPC32BitTester {
	MachineArchitecture createSubject(String name);

	@ParameterizedTest(name = "has 32-bit pointer size [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.MachineArchitectureTestUtils#commonPowerPC32BitNames")
	default void powerPC32BitArchitectureHas32BitPointerSize(String name) {
		assertAll(
			() -> assertThat(createSubject(name).is32Bit(), is(true)),
			() -> assertThat(createSubject(name).is64Bit(), is(false))
		);
	}

	@ParameterizedTest(name = "has canonical hame [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.MachineArchitectureTestUtils#commonPowerPC32BitNames")
	default void powerPC32BitArchitectureHasCanonicalName(String name) {
		assertThat(createSubject(name), named("PowerPC"));
	}
}
