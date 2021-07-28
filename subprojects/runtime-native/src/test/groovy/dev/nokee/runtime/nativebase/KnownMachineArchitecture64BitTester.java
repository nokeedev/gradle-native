package dev.nokee.runtime.nativebase;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static dev.nokee.runtime.nativebase.MachineArchitectureTestUtils.common64BitMachineArchitectures;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
interface KnownMachineArchitecture64BitTester {
	MachineArchitecture createSubject(String name);

	default Stream<MachineArchitectureUnderTest> provideKnown64BitMachineArchitecturesUnderTest() {
		return common64BitMachineArchitectures();
	}

	@ParameterizedTest(name = "has 64-bit pointer size [{arguments}]")
	@MethodSource("provideKnown64BitMachineArchitecturesUnderTest")
	default void has64BitPointerSize(MachineArchitectureUnderTest architecture) {
		assertAll(
			() -> assertThat(createSubject(architecture.getName()).is32Bit(), is(false)),
			() -> assertThat(createSubject(architecture.getName()).is64Bit(), is(true))
		);
	}
}
