package dev.nokee.runtime.nativebase;

import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface UnknownMachineArchitectureTester {
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
