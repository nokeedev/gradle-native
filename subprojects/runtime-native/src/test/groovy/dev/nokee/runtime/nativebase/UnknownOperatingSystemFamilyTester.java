package dev.nokee.runtime.nativebase;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

interface UnknownOperatingSystemFamilyTester {
	OperatingSystemFamily createSubject(String name);

	@Test
	default void unknownFamilyNameIsNotAnyOfKnownFamilyType() {
		val subject = createSubject("unknown-os");
		assertAll(
			() -> assertFalse(subject.isWindows(), "windows family"),
			() -> assertFalse(subject.isFreeBSD(), "freeBSD family"),
			() -> assertFalse(subject.isLinux(), "linux family"),
			() -> assertFalse(subject.isMacOs(), "macOS family"),
			() -> assertFalse(subject.isIos(), "iOS family"),
			() -> assertFalse(subject.isHewlettPackardUnix(), "HP-UX family"),
			() -> assertFalse(subject.isSolaris(), "solaris family")
		);
	}
}
