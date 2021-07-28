package dev.nokee.runtime.nativebase;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface KnownOperatingSystemFamilyTester {
	OperatingSystemFamily createSubject(String name);

	default Stream<OperatingSystemFamilyUnderTest> provideKnownOperatingSystemFamiliesUnderTest() {
		return OperatingSystemFamilyTestUtils.knownOperatingSystemFamilies();
	}

	@ParameterizedTest(name = "can detect known OS family type [{arguments}]")
	@MethodSource("provideKnownOperatingSystemFamiliesUnderTest")
	default void canDetectKnownOperatingSystemFamilyType(OperatingSystemFamilyUnderTest osFamily) {
		osFamily.assertKnownOperatingSystemFamilyDetected(createSubject(osFamily.getName()));
	}
}
