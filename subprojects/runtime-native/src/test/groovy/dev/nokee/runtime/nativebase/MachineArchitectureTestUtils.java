package dev.nokee.runtime.nativebase;

import com.google.common.collect.Streams;

import java.util.stream.Stream;

import static dev.nokee.runtime.nativebase.MachineArchitectureUnderTest.canonical;

/**
 * NOTE: Common architecture names doesn't covert all possible names that Nokee accepts but general variant an user may use.
 * As the Nokee code errors out on the lenient side, additional case mixing can be accepted than the one tested here.
 */
final class MachineArchitectureTestUtils {
	private MachineArchitectureTestUtils() {}

	static Stream<MachineArchitectureUnderTest> common32BitMachineArchitectures() {
		return Streams.concat(commonIntel32BitArchitectures(), commonPowerPC32BitArchitectures(), commonSparc32BitArchitectures());
	}

	static Stream<MachineArchitectureUnderTest> common64BitMachineArchitectures() {
		return Streams.concat(commonItaniumArchitectures(), commonIntel64BitArchitectures(), commonPowerPC64BitArchitectures(),
				commonSparc64BitArchitectures(), commonHPPARISCArchitectures());
	}

	static Stream<MachineArchitectureUnderTest> commonItaniumArchitectures() {
		return Stream.of("IA-64", "ia-64", "IA64", "IA64N", "Itanium", "itanium").map(canonical("ia-64")::withName);
	}

	static Stream<MachineArchitectureUnderTest> commonIntel32BitArchitectures() {
		return Stream.of("x86", "i386", "i686").map(canonical("x86")::withName);
	}

	static Stream<MachineArchitectureUnderTest> commonIntel64BitArchitectures() {
		return Stream.of("x86_64", "x86-64", "x64", "amd64", "AMD64").map(canonical("x86-64")::withName);
	}

	static Stream<MachineArchitectureUnderTest> commonPowerPC32BitArchitectures() {
		return Stream.of("ppc", "PowerPC").map(canonical("ppc")::withName);
	}

	static Stream<MachineArchitectureUnderTest> commonPowerPC64BitArchitectures() {
		return Stream.of("ppc64", "PowerPC64").map(canonical("ppc64")::withName);
	}

	static Stream<MachineArchitectureUnderTest> commonSparc32BitArchitectures() {
		return Stream.of("sparc", "SPARC", "SPARC-V7", "SPARC-V8").map(canonical("sparc")::withName);
	}

	static Stream<MachineArchitectureUnderTest> commonSparc64BitArchitectures() {
		return Stream.of("sparc64", "SPARC64", "SPARC-V9", "ultrasparc", "UltraSPARC").map(canonical("sparc64")::withName);
	}

	static Stream<MachineArchitectureUnderTest> commonHPPARISCArchitectures() {
		return Stream.of("PA_RISC", "PA-RISC").map(canonical("pa-risc")::withName);
	}
}
