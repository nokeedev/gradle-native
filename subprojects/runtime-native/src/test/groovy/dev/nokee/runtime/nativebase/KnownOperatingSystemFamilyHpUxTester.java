package dev.nokee.runtime.nativebase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

/** @see KnownOperatingSystemFamilyTester */
interface KnownOperatingSystemFamilyHpUxTester {
	OperatingSystemFamily createSubject(String name);

	@ParameterizedTest(name = "can detect OS family type [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.OperatingSystemFamilyTestUtils#commonHPUXNames")
	default void hpUXFamilyNameDetected(String name) {
		assertAll(
			() -> assertThat("windows family", createSubject(name).isWindows(), is(false)),
			() -> assertThat("freeBSD family", createSubject(name).isFreeBSD(), is(false)),
			() -> assertThat("linux family", createSubject(name).isLinux(), is(false)),
			() -> assertThat("macOS family", createSubject(name).isMacOs(), is(false)),
			() -> assertThat("iOS family", createSubject(name).isIos(), is(false)),
			() -> assertThat("HP-UX family", createSubject(name).isHewlettPackardUnix(), is(true)),
			() -> assertThat("solaris family", createSubject(name).isSolaris(), is(false))
		);
	}

	@ParameterizedTest(name = "has canonical name [{arguments}]")
	@MethodSource("dev.nokee.runtime.nativebase.OperatingSystemFamilyTestUtils#commonHPUXNames")
	default void hpUxArchitectureHasCanonicalName(String name) {
		assertThat(createSubject(name).getCanonicalName(), equalTo("hpux"));
	}
}
