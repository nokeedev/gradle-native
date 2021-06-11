package dev.nokee.runtime.nativebase;

import java.util.stream.Stream;

/**
 * NOTE: Common architecture names doesn't covert all possible names that Nokee accepts but general variant an user may use.
 * As the Nokee code errors out on the lenient side, additional case mixing can be accepted than the one tested here.
 */
final class OperatingSystemFamilyTestUtils {
	private OperatingSystemFamilyTestUtils() {}

	static Stream<String> commonWindowsNames() {
		return Stream.of("windows", "Windows");
	}

	static Stream<String> commonMacOSNames() {
		return Stream.of("macos", "macosx", "macOS", "darwin", "osx");
	}

	static Stream<String> commonLinuxNames() {
		return Stream.of("linux");
	}

	static Stream<String> commonHPUXNames() {
		return Stream.of("hp-ux", "HP-UX", "HPUX");
	}

	static Stream<String> commonSolarisNames() {
		return Stream.of("sunos", "SunOS", "Solaris", "solaris");
	}

	static Stream<String> commonFreeBSDNames() {
		return Stream.of("freebsd", "FreeBSD");
	}

	static Stream<String> commonIosNames() {
		return Stream.of("iOS", "ios");
	}
}
