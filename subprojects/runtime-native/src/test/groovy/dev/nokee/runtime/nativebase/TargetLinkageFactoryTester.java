package dev.nokee.runtime.nativebase;

import com.google.common.testing.EqualsTester;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

public interface TargetLinkageFactoryTester {
	TargetLinkageFactory subject();

	@Test
	default void canCreateSharedLinkage() {
		assertThat(subject().getShared(), isA(TargetLinkage.class));
		assertThat(subject().getShared(), is(TargetLinkages.SHARED));
	}

	@Test
	default void canCreateStaticLinkage() {
		assertThat(subject().getStatic(), isA(TargetLinkage.class));
		assertThat(subject().getStatic(), is(TargetLinkages.STATIC));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(subject().getShared(), subject().getShared())
			.addEqualityGroup(subject().getStatic(), subject().getStatic())
			.testEquals();
	}
}
