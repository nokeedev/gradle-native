package dev.nokee.runtime.nativebase;

import com.google.common.collect.Streams;
import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.nativebase.MachineArchitecture.forName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class MachineArchitectureTest {
	@Nested
	class ObjectFactoryTest implements NamedValueTester<MachineArchitecture>, KnownMachineArchitectureTester, UnknownMachineArchitectureTester {
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
	class MethodFactoryTest implements NamedValueTester<MachineArchitecture>, KnownMachineArchitectureTester, UnknownMachineArchitectureTester {
		@Override
		public MachineArchitecture createSubject(String name) {
			return forName(name);
		}

		@Override
		public Stream<String> knownValues() {
			return knownMachineArchitectures();
		}
	}

	private static Stream<String> knownMachineArchitectures() {
		return Streams.concat(
			MachineArchitectureTestUtils.commonHPPARISCNames(),
			MachineArchitectureTestUtils.commonIntel32BitNames(),
			MachineArchitectureTestUtils.commonIntel64BitNames(),
			MachineArchitectureTestUtils.commonItaniumNames(),
			MachineArchitectureTestUtils.commonPowerPC32BitNames(),
			MachineArchitectureTestUtils.commonPowerPC64BitNames(),
			MachineArchitectureTestUtils.commonSparc32BitNames(),
			MachineArchitectureTestUtils.commonSparc64BitNames()
		);
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
