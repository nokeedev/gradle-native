package dev.nokee.runtime.nativebase.internal;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class WellKnownTargetLinkageInstancesTest {
	@Test
	void checkSharedLibraryLinkage() {
		assertThat(TargetLinkages.SHARED, hasToString("shared"));
	}

	@Test
	void checkStaticLibraryLinkage() {
		assertThat(TargetLinkages.STATIC, hasToString("static"));
	}

	@Test
	void checkBundleLinkage() {
		assertThat(TargetLinkages.BUNDLE, hasToString("bundle"));
	}

	@Test
	void checkExecutableLinkage() {
		assertThat(TargetLinkages.EXECUTABLE, hasToString("executable"));
	}
}
