package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class TargetBuildTypeFactoryTest {
	TargetBuildTypeFactory createSubject() {
		return NativeRuntimeBasePlugin.TARGET_BUILD_TYPE_FACTORY;
	}

	@Test
	void canCreateBuildType() {
		assertThat(createSubject().named("debug"), isA(TargetBuildType.class));
	}
}
