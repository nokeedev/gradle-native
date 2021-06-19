package dev.nokee.runtime.nativebase;

import com.google.common.collect.Streams;
import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.forName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class OperatingSystemFamilyTest {
	@Nested
	class ObjectFactoryTest implements NamedValueTester<OperatingSystemFamily>, KnownOperatingSystemFamilyTester, UnknownOperatingSystemFamilyTester {
		@Override
		public OperatingSystemFamily createSubject(String name) {
			return objectFactory().named(OperatingSystemFamily.class, name);
		}

		@Override
		public Stream<String> knownValues() {
			return knownOperatingSystemFamilies();
		}
	}

	@Nested
	class MethodFactoryTest implements NamedValueTester<OperatingSystemFamily>, KnownOperatingSystemFamilyTester, UnknownOperatingSystemFamilyTester {
		@Override
		public OperatingSystemFamily createSubject(String name) {
			return forName(name);
		}

		@Override
		public Stream<String> knownValues() {
			return knownOperatingSystemFamilies();
		}
	}

	private static Stream<String> knownOperatingSystemFamilies() {
		return Streams.concat(
			OperatingSystemFamilyTestUtils.commonFreeBSDNames(),
			OperatingSystemFamilyTestUtils.commonHPUXNames(),
			OperatingSystemFamilyTestUtils.commonIosNames(),
			OperatingSystemFamilyTestUtils.commonLinuxNames(),
			OperatingSystemFamilyTestUtils.commonMacOSNames(),
			OperatingSystemFamilyTestUtils.commonSolarisNames(),
			OperatingSystemFamilyTestUtils.commonWindowsNames()
		);
	}

	@Test
	void hasWindowsOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.WINDOWS, equalTo("windows"));
	}

	@Test
	void hasLinuxOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.LINUX, equalTo("linux"));
	}

	@Test
	void hasMacOsOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.MACOS, equalTo("macos"));
	}

	@Test
	void hasSolarisOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.SOLARIS, equalTo("solaris"));
	}

	@Test
	void hasIosOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.IOS, equalTo("ios"));
	}

	@Test
	void hasFreeBsdOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.FREE_BSD, equalTo("freebsd"));
	}

	@Test
	void hasHpUxOperatingSystemFamilyCanonicalName() {
		assertThat(OperatingSystemFamily.HP_UX, equalTo("hpux"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualityAgainstObjectFactoryNamedInstance() {
		new EqualsTester()
			.addEqualityGroup((Object[]) equalityGroupFor("windows"))
			.addEqualityGroup((Object[]) equalityGroupFor("MacOS"))
			.addEqualityGroup((Object[]) equalityGroupFor("some-os"))
			.addEqualityGroup((Object[]) equalityGroupFor("Another-OS"))
			.testEquals();
	}

	private static OperatingSystemFamily[] equalityGroupFor(String name) {
		return new OperatingSystemFamily[] {
			objectFactory().named(OperatingSystemFamily.class, name),
			forName(name),
			objectFactory().named(OperatingSystemFamily.class, name)
		};
	}
}
