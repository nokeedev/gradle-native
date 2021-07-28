package dev.nokee.runtime.nativebase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.runtime.nativebase.OperatingSystemFamilyTestUtils.knownOperatingSystemFamilies;
import static org.hamcrest.MatcherAssert.assertThat;

public interface OperatingSystemFamilyTester extends NamedValueTester<OperatingSystemFamily>, KnownOperatingSystemFamilyTester, UnknownOperatingSystemFamilyTester {
	default Stream<OperatingSystemFamilyUnderTest> provideOperatingSystemFamiliesUnderTest() {
		return knownOperatingSystemFamilies();
	}

	@Override
	default Stream<String> knownValues() {
		return knownOperatingSystemFamilies().map(OperatingSystemFamilyUnderTest::getName);
	}

	@ParameterizedTest(name = "has name [{arguments}]")
	@MethodSource("provideOperatingSystemFamiliesUnderTest")
	default void hasName(OperatingSystemFamilyUnderTest osFamily) {
		assertThat(createSubject(osFamily.getName()), named(osFamily.getName()));
	}

	@ParameterizedTest(name = "has canonical name [{arguments}]")
	@MethodSource("provideOperatingSystemFamiliesUnderTest")
	default void hasCanonicalName(OperatingSystemFamilyUnderTest osFamily) {
		osFamily.assertCanonicalName(createSubject(osFamily.getName()));
	}
}
