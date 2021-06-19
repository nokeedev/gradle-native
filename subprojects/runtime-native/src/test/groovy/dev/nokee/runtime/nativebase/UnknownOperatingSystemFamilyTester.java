package dev.nokee.runtime.nativebase;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface UnknownOperatingSystemFamilyTester {
	OperatingSystemFamily createSubject(String name);

	@Test
	default void unknownFamilyNameIsNotAnyOfCommonFamily() {
		val subject = createSubject("unknown-os");
		assertAll(
			() -> assertThat("windows family", subject.isWindows(), is(false)),
			() -> assertThat("freeBSD family", subject.isFreeBSD(), is(false)),
			() -> assertThat("linux family", subject.isLinux(), is(false)),
			() -> assertThat("macOS family", subject.isMacOs(), is(false)),
			() -> assertThat("iOS family", subject.isIos(), is(false)),
			() -> assertThat("HP-UX family", subject.isHewlettPackardUnix(), is(false)),
			() -> assertThat("solaris family", subject.isSolaris(), is(false))
		);
	}
}
