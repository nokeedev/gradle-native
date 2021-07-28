package dev.nokee.runtime.nativebase;

import com.google.common.collect.Streams;
import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.nativebase.MachineArchitecture.forName;
import static dev.nokee.runtime.nativebase.MachineArchitecture.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class MachineArchitectureTest {
	@Nested
	class ObjectFactoryTest implements NamedValueTester<MachineArchitecture>, MachineArchitectureTester {
		@Override
		public MachineArchitecture createSubject(String name) {
			return objectFactory().named(MachineArchitecture.class, name);
		}

		@Override
		public Stream<String> knownValues() {
			return knownMachineArchitectures();
		}
	}

	@Nested
	class MethodFactoryTest implements NamedValueTester<MachineArchitecture>, MachineArchitectureTester {
		@Override
		public MachineArchitecture createSubject(String name) {
			return named(name);
		}

		@Override
		public Stream<String> knownValues() {
			return knownMachineArchitectures();
		}
	}

	private static Stream<String> knownMachineArchitectures() {
		return Streams.concat(
			MachineArchitectureTestUtils.commonHPPARISCArchitectures(),
			MachineArchitectureTestUtils.commonIntel32BitArchitectures(),
			MachineArchitectureTestUtils.commonIntel64BitArchitectures(),
			MachineArchitectureTestUtils.commonItaniumArchitectures(),
			MachineArchitectureTestUtils.commonPowerPC32BitArchitectures(),
			MachineArchitectureTestUtils.commonPowerPC64BitArchitectures(),
			MachineArchitectureTestUtils.commonSparc32BitArchitectures(),
			MachineArchitectureTestUtils.commonSparc64BitArchitectures()
		).map(MachineArchitectureUnderTest::getName);
	}

	@Test
	void hasIntel32BitMachineArchitectureCanonicalName() {
		assertThat(MachineArchitecture.X86, equalTo("x86"));
	}

	@Test
	void hasIntel64BitMachineArchitectureCanonicalName() {
		assertThat(MachineArchitecture.X86_64, equalTo("x86-64"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualityAgainstObjectFactoryNamedInstance() {
		new EqualsTester()
			.addEqualityGroup((Object[]) equalityGroupFor("x86"))
			.addEqualityGroup((Object[]) equalityGroupFor("X86-64"))
			.addEqualityGroup((Object[]) equalityGroupFor("some-arch"))
			.addEqualityGroup((Object[]) equalityGroupFor("Another-Arch"))
			.testEquals();
	}

	private static MachineArchitecture[] equalityGroupFor(String name) {
		return new MachineArchitecture[] {
			objectFactory().named(MachineArchitecture.class, name),
			forName(name),
			objectFactory().named(MachineArchitecture.class, name)
		};
	}
}
