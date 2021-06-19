package dev.nokee.runtime.nativebase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

/** @see KnownOperatingSystemFamilyTester */
interface KnownOperatingSystemFamilyFreeBsbTester {
	OperatingSystemFamily createSubject(String name);

	@ParameterizedTest(name = "can detect OS family type [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.OperatingSystemFamilyTestUtils#commonFreeBSDNames")
	default void freeBsdFamilyNameDetected(String name) {
		assertAll(
			() -> assertThat("windows family", createSubject(name).isWindows(), is(false)),
			() -> assertThat("freeBSD family", createSubject(name).isFreeBSD(), is(true)),
			() -> assertThat("linux family", createSubject(name).isLinux(), is(false)),
			() -> assertThat("macOS family", createSubject(name).isMacOs(), is(false)),
			() -> assertThat("iOS family", createSubject(name).isIos(), is(false)),
			() -> assertThat("HP-UX family", createSubject(name).isHewlettPackardUnix(), is(false)),
			() -> assertThat("solaris family", createSubject(name).isSolaris(), is(false))
		);
	}

	@ParameterizedTest(name = "has canonical name [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.OperatingSystemFamilyTestUtils#commonFreeBSDNames")
	default void freeBsdArchitectureHasCanonicalName(String name) {
		assertThat(createSubject(name).getCanonicalName(), equalTo("freebsd"));
	}
}
