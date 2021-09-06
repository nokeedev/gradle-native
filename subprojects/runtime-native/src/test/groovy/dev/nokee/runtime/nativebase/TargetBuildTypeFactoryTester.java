package dev.nokee.runtime.nativebase;

import com.google.common.testing.EqualsTester;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

public interface TargetBuildTypeFactoryTester {
	TargetBuildTypeFactory subject();

	@Test
	default void canCreateBuildType() {
		assertThat(subject().named("debug"), isA(TargetBuildType.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(subject().named("debug"), subject().named("debug"))
			.addEqualityGroup(subject().named("release"))
			.testEquals();
	}
}
