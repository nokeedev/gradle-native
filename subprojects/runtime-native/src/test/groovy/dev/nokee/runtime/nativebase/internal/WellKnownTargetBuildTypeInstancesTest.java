package dev.nokee.runtime.nativebase.internal;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class WellKnownTargetBuildTypeInstancesTest {
	@Test
	void checkDefaultBuildType() {
		assertThat(TargetBuildTypes.DEFAULT, hasToString("default"));
	}
}
