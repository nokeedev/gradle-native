/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
