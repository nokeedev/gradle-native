package dev.nokee.runtime.nativebase;

import java.util.stream.Stream;

/**
 * NOTE: Common architecture names doesn't covert all possible names that Nokee accepts but general variant an user may use.
 * As the Nokee code errors out on the lenient side, additional case mixing can be accepted than the one tested here.
 */
final class MachineArchitectureTestUtils {
	private MachineArchitectureTestUtils() {}

	static Stream<String> commonItaniumNames() {
		return Stream.of("IA-64", "ia-64", "IA64", "IA64N", "Itanium", "itanium");
	}

	static Stream<String> commonIntel32BitNames() {
		return Stream.of("x86", "i386", "i686");
	}

	static Stream<String> commonIntel64BitNames() {
		return Stream.of("x86_64", "x86-64", "x64", "amd64", "AMD64");
	}

	static Stream<String> commonPowerPC32BitNames() {
		return Stream.of("ppc", "PowerPC");
	}

	static Stream<String> commonPowerPC64BitNames() {
		return Stream.of("ppc64", "PowerPC64");
	}

	static Stream<String> commonSparc32BitNames() {
		return Stream.of("sparc", "SPARC", "SPARC-V7", "SPARC-V8");
	}

	static Stream<String> commonSparc64BitNames() {
		return Stream.of("sparc64", "SPARC64", "SPARC-V9", "ultrasparc", "UltraSPARC");
	}

	static Stream<String> commonHPPARISCNames() {
		return Stream.of("PA_RISC", "PA-RISC");
	}
}
