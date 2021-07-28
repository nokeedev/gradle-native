package dev.nokee.runtime.nativebase;

import com.google.common.collect.Streams;

import java.util.stream.Stream;

import static dev.nokee.runtime.nativebase.OperatingSystemFamilyUnderTest.*;

/**
 * NOTE: Common operating system names doesn't covert all possible names that Nokee accepts but general variant an user may use.
 * As the Nokee code errors out on the lenient side, additional case mixing can be accepted than the one tested here.
 */
final class OperatingSystemFamilyTestUtils {
	private OperatingSystemFamilyTestUtils() {}

	static Stream<OperatingSystemFamilyUnderTest> knownOperatingSystemFamilies() {
		return Streams.concat(commonWindowsFamilies(), commonMacOSFamilies(), commonLinuxFamilies(),
			commonHPUXFamilies(), commonSolarisFamilies(), commonFreeBSDFamilies(), commonIosFamilies()
		);
	}

	static Stream<OperatingSystemFamilyUnderTest> commonWindowsFamilies() {
		return Stream.of("windows", "Windows").map(windows()::withName);
	}

	static Stream<OperatingSystemFamilyUnderTest> commonMacOSFamilies() {
		return Stream.of("macos", "macosx", "macOS", "darwin", "osx").map(macOS()::withName);
	}

	static Stream<OperatingSystemFamilyUnderTest> commonLinuxFamilies() {
		return Stream.of("linux").map(linux()::withName);
	}

	static Stream<OperatingSystemFamilyUnderTest> commonHPUXFamilies() {
		return Stream.of("hp-ux", "HP-UX", "HPUX").map(hpUx()::withName);
	}

	static Stream<OperatingSystemFamilyUnderTest> commonSolarisFamilies() {
		return Stream.of("sunos", "SunOS", "Solaris", "solaris").map(solaris()::withName);
	}

	static Stream<OperatingSystemFamilyUnderTest> commonFreeBSDFamilies() {
		return Stream.of("freebsd", "FreeBSD").map(freeBSD()::withName);
	}

	static Stream<OperatingSystemFamilyUnderTest> commonIosFamilies() {
		return Stream.of("iOS", "ios").map(iOS()::withName);
	}
}
