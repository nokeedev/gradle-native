package dev.nokee.runtime.nativebase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

/** @see KnownMachineArchitectureTester */
interface KnownMachineArchitectureSparc64BitTester {
	MachineArchitecture createSubject(String name);

	@ParameterizedTest(name = "has 64-bit pointer size [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.MachineArchitectureTestUtils#commonSparc64BitNames")
	default void sparc64BitArchitectureHas64BitPointerSize(String name) {
		assertAll(
			() -> assertThat(createSubject(name).is32Bit(), is(false)),
			() -> assertThat(createSubject(name).is64Bit(), is(true))
		);
	}

	@ParameterizedTest(name = "has canonical name [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.MachineArchitectureTestUtils#commonSparc64BitNames")
	default void sparc64BitArchitectureHasCanonicalName(String name) {
		assertThat(createSubject(name).getCanonicalName(), equalTo("sparc64"));
	}
}
