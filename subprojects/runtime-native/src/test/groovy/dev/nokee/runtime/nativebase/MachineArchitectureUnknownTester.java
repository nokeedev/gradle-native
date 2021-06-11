package dev.nokee.runtime.nativebase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** @see dev.nokee.runtime.nativebase.CommonMachineArchitectureTester */
interface MachineArchitectureUnknownTester {
	MachineArchitecture createSubject(String name);

	@Test
	default void throwsExceptionWhenChecking32BitPointerSizeDueToUnknownArchitectureNae() {
		assertThrows(UnsupportedOperationException.class, () -> createSubject("unknown-arch").is32Bit());
	}

	@Test
	default void throwsExceptionWhenChecking64BitPointerSizeDueToUnknownArchitectureNae() {
		assertThrows(UnsupportedOperationException.class, () -> createSubject("unknown-arch").is64Bit());
	}

	@Test
	default void usesArchitectureNameAsIsButLoweredCaseWhenUnknown() {
		assertAll(
			() -> assertThat(createSubject("unknown-arch"), named("unknown-arch")),
			() -> assertThat(createSubject("Unknown-ARCH"), named("unknown-arch"))
		);
	}
}
