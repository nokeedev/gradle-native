package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.nativebase.MachineArchitecture;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

interface MachineArchitectureItaniumTester {
	MachineArchitecture createSubject(String name);

	@ParameterizedTest(name = "has 64-bit pointer size [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.MachineArchitectureTestUtils#commonItaniumNames")
	default void itaniumArchitectureHas64BitPointerSize(String name) {
		assertAll(
			() -> assertThat(createSubject(name).is32Bit(), is(false)),
			() -> assertThat(createSubject(name).is64Bit(), is(true))
		);
	}

	@ParameterizedTest(name = "has canonical hame [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.MachineArchitectureTestUtils#commonItaniumNames")
	default void itaniumArchitectureHasCanonicalName(String name) {
		assertThat(createSubject(name), named("Itanium"));
	}
}
