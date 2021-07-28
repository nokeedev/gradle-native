package dev.nokee.runtime.nativebase;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static dev.nokee.runtime.nativebase.MachineArchitectureTestUtils.common32BitMachineArchitectures;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
interface KnownMachineArchitecture32BitTester {
	MachineArchitecture createSubject(String name);

	default Stream<MachineArchitectureUnderTest> provideKnown32BitMachineArchitecturesUnderTest() {
		return common32BitMachineArchitectures();
	}

	@ParameterizedTest(name = "has 32-bit pointer size [{arguments}]")
	@MethodSource("provideKnown32BitMachineArchitecturesUnderTest")
	default void has32BitPointerSize(MachineArchitectureUnderTest architecture) {
		assertAll(
			() -> assertThat(createSubject(architecture.getName()).is32Bit(), is(true)),
			() -> assertThat(createSubject(architecture.getName()).is64Bit(), is(false))
		);
	}
}
