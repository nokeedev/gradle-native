package dev.nokee.runtime.nativebase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public final class OperatingSystemFamilyUnderTest {
	private final String name;
	private final CanonicalOperatingSystemFamily knownOSFamily;

	private OperatingSystemFamilyUnderTest(String name, CanonicalOperatingSystemFamily knownOSFamily) {
		this.name = name;
		this.knownOSFamily = knownOSFamily;
	}

	public String getName() {
		return name;
	}

	String getCanonicalName() {
		return knownOSFamily.getCanonicalName();
	}

	public OperatingSystemFamilyUnderTest withName(String name) {
		return new OperatingSystemFamilyUnderTest(name, knownOSFamily);
	}

	private static OperatingSystemFamilyUnderTest ofKnown(KnownOperatingSystemFamilies knownOSFamily) {
		return new OperatingSystemFamilyUnderTest(knownOSFamily.getCanonicalName(), knownOSFamily);
	}

	public static OperatingSystemFamilyUnderTest windows() {
		return ofKnown(KnownOperatingSystemFamilies.WINDOWS);
	}

	public static OperatingSystemFamilyUnderTest linux() {
		return ofKnown(KnownOperatingSystemFamilies.LINUX);
	}

	public static OperatingSystemFamilyUnderTest macOS() {
		return ofKnown(KnownOperatingSystemFamilies.MACOS);
	}

	public static OperatingSystemFamilyUnderTest hpUx() {
		return ofKnown(KnownOperatingSystemFamilies.HP_UX);
	}

	public static OperatingSystemFamilyUnderTest solaris() {
		return ofKnown(KnownOperatingSystemFamilies.SOLARIS);
	}

	public static OperatingSystemFamilyUnderTest freeBSD() {
		return ofKnown(KnownOperatingSystemFamilies.FREE_BSD);
	}

	public static OperatingSystemFamilyUnderTest iOS() {
		return ofKnown(KnownOperatingSystemFamilies.IOS);
	}

	@Override
	public String toString() {
		return getName();
	}

	public void assertCanonicalName(OperatingSystemFamily subject) {
		assertThat(subject.getCanonicalName(), equalTo(knownOSFamily.getCanonicalName()));
	}

	public void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject) {
		knownOSFamily.assertKnownOperatingSystemFamilyDetected(subject);
	}

	private interface CanonicalOperatingSystemFamily {
		String getCanonicalName();
		void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject);
	}

	private enum KnownOperatingSystemFamilies implements CanonicalOperatingSystemFamily {
		WINDOWS("windows") {
			@Override
			public void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject) {
				assertAll(
					() -> assertTrue(subject.isWindows(), "windows family"),
					() -> assertFalse(subject.isFreeBSD(), "freeBSD family"),
					() -> assertFalse(subject.isLinux(), "linux family"),
					() -> assertFalse(subject.isMacOs(), "macOS family"),
					() -> assertFalse(subject.isIos(), "iOS family"),
					() -> assertFalse(subject.isHewlettPackardUnix(), "HP-UX family"),
					() -> assertFalse(subject.isSolaris(), "solaris family")
				);
			}
		},
		LINUX("linux") {
			@Override
			public void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject) {
				assertAll(
					() -> assertFalse(subject.isWindows(), "windows family"),
					() -> assertFalse(subject.isFreeBSD(), "freeBSD family"),
					() -> assertTrue(subject.isLinux(), "linux family"),
					() -> assertFalse(subject.isMacOs(), "macOS family"),
					() -> assertFalse(subject.isIos(), "iOS family"),
					() -> assertFalse(subject.isHewlettPackardUnix(), "HP-UX family"),
					() -> assertFalse(subject.isSolaris(), "solaris family")
				);
			}
		},
		MACOS("macos") {
			@Override
			public void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject) {
				assertAll(
					() -> assertFalse(subject.isWindows(), "windows family"),
					() -> assertFalse(subject.isFreeBSD(), "freeBSD family"),
					() -> assertFalse(subject.isLinux(), "linux family"),
					() -> assertTrue(subject.isMacOs(), "macOS family"),
					() -> assertFalse(subject.isIos(), "iOS family"),
					() -> assertFalse(subject.isHewlettPackardUnix(), "HP-UX family"),
					() -> assertFalse(subject.isSolaris(), "solaris family")
				);
			}
		},
		HP_UX("hpux") {
			@Override
			public void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject) {
				assertAll(
					() -> assertFalse(subject.isWindows(), "windows family"),
					() -> assertFalse(subject.isFreeBSD(), "freeBSD family"),
					() -> assertFalse(subject.isLinux(), "linux family"),
					() -> assertFalse(subject.isMacOs(), "macOS family"),
					() -> assertFalse(subject.isIos(), "iOS family"),
					() -> assertTrue(subject.isHewlettPackardUnix(), "HP-UX family"),
					() -> assertFalse(subject.isSolaris(), "solaris family")
				);
			}
		},
		SOLARIS("solaris") {
			@Override
			public void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject) {
				assertAll(
					() -> assertFalse(subject.isWindows(), "windows family"),
					() -> assertFalse(subject.isFreeBSD(), "freeBSD family"),
					() -> assertFalse(subject.isLinux(), "linux family"),
					() -> assertFalse(subject.isMacOs(), "macOS family"),
					() -> assertFalse(subject.isIos(), "iOS family"),
					() -> assertFalse(subject.isHewlettPackardUnix(), "HP-UX family"),
					() -> assertTrue(subject.isSolaris(), "solaris family")
				);
			}
		},
		FREE_BSD("freebsd") {
			@Override
			public void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject) {
				assertAll(
					() -> assertFalse(subject.isWindows(), "windows family"),
					() -> assertTrue(subject.isFreeBSD(), "freeBSD family"),
					() -> assertFalse(subject.isLinux(), "linux family"),
					() -> assertFalse(subject.isMacOs(), "macOS family"),
					() -> assertFalse(subject.isIos(), "iOS family"),
					() -> assertFalse(subject.isHewlettPackardUnix(), "HP-UX family"),
					() -> assertFalse(subject.isSolaris(), "solaris family")
				);
			}
		},
		IOS("ios") {
			@Override
			public void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject) {
				assertAll(
					() -> assertFalse(subject.isWindows(), "windows family"),
					() -> assertFalse(subject.isFreeBSD(), "freeBSD family"),
					() -> assertFalse(subject.isLinux(), "linux family"),
					() -> assertFalse(subject.isMacOs(), "macOS family"),
					() -> assertTrue(subject.isIos(), "iOS family"),
					() -> assertFalse(subject.isHewlettPackardUnix(), "HP-UX family"),
					() -> assertFalse(subject.isSolaris(), "solaris family")
				);
			}
		};

		private final String canonicalName;

		KnownOperatingSystemFamilies(String canonicalName) {
			this.canonicalName = canonicalName;
		}

		public String getCanonicalName() {
			return canonicalName;
		}

		public abstract void assertKnownOperatingSystemFamilyDetected(OperatingSystemFamily subject);
	}
}
