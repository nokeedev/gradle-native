package dev.nokee.runtime.nativebase;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public interface TargetMachineTester {
	TargetMachine subject();

	@Test
	default void hasOperatingSystemFamily() {
		assertThat(subject().getOperatingSystemFamily(), notNullValue());
	}

	@Test
	default void hasMachineArchitecture() {
		assertThat(subject().getArchitecture(), notNullValue());
	}
}
