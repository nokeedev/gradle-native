package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class TargetLinkageFactoryTest {
	TargetLinkageFactory createSubject() {
		return NativeRuntimePlugin.TARGET_LINKAGE_FACTORY;
	}

	@Test
	void canCreateSharedLinkage() {
		assertThat(createSubject().getShared(), isA(TargetLinkage.class));
		assertThat(createSubject().getShared(), is(TargetLinkages.SHARED));
	}

	@Test
	void canCreateStaticLinkage() {
		assertThat(createSubject().getStatic(), isA(TargetLinkage.class));
		assertThat(createSubject().getStatic(), is(TargetLinkages.STATIC));
	}
}
