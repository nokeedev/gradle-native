package dev.nokee.runtime.nativebase.internal;

import com.google.common.testing.EqualsTester;
import dev.nokee.runtime.nativebase.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

class DefaultTargetMachineFactoryTest {
	private final DefaultTargetMachineFactory factory = new DefaultTargetMachineFactory();

	@Test
	void defaultsToTheCurrentArchitecture() {
		// TODO Improve when we test on other architectures
		assertAll(
			() -> assertThat(factory.getWindows().getArchitecture().getCanonicalName(), equalTo(MachineArchitecture.X86_64)),
			() -> assertThat(factory.getLinux().getArchitecture().getCanonicalName(), equalTo(MachineArchitecture.X86_64)),
			() -> assertThat(factory.getMacOS().getArchitecture().getCanonicalName(), equalTo(MachineArchitecture.X86_64))
		);
	}

	@Test
	void configureTheRightOperatingSystemFamily() {
		assertAll(
			() -> assertThat(factory.getWindows().getOperatingSystemFamily(), equalTo(DefaultOperatingSystemFamily.WINDOWS)),
			() -> assertThat(factory.getLinux().getOperatingSystemFamily(), equalTo(DefaultOperatingSystemFamily.LINUX)),
			() -> assertThat(factory.getMacOS().getOperatingSystemFamily(), equalTo(DefaultOperatingSystemFamily.MACOS))
		);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void canCompareInstances() {
		new EqualsTester()
			.addEqualityGroup(factory.getWindows(), factory.getWindows())
			.addEqualityGroup(factory.getLinux())
			.addEqualityGroup(factory.getMacOS(), new DefaultTargetMachine(DefaultOperatingSystemFamily.MACOS, MachineArchitecture.forName(System.getProperty("os.arch"))))
			.testEquals();
	}

	@Test
	@EnabledOnOs(OS.LINUX)
	void defaultsToTheRightPreMadeInstancesOnLinux() {
		assertThat(factory.host().getOperatingSystemFamily(), equalTo(DefaultOperatingSystemFamily.LINUX));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void defaultsToTheRightPreMadeInstancesOnWindows() {
		assertThat(factory.host().getOperatingSystemFamily(), equalTo(DefaultOperatingSystemFamily.WINDOWS));
	}

	@Test
	@EnabledOnOs(OS.MAC)
	void defaultsToTheRightPreMadeInstancesOnMacOS() {
		assertThat(factory.host().getOperatingSystemFamily(), equalTo(DefaultOperatingSystemFamily.MACOS));
	}

	@Test
	void defaultsToTheRightPreMadeInstances() {
		// TODO Improve when we test on other architectures
		assertThat(factory.host().getArchitecture().getCanonicalName(), equalTo(MachineArchitecture.X86_64));
	}

	@Nested
	class CommonMachineArchitectureTest implements KnownMachineArchitectureTester, UnknownMachineArchitectureTester {
		@Override
		public MachineArchitecture createSubject(String name) {
			return factory.os("some-os").architecture(name).getArchitecture();
		}
	}

	@Nested
	class CommonOperatingSystemFamilyTest implements CommonOperatingSystemFamilyTester {
		@Override
		public OperatingSystemFamily createSubject(String name) {
			return factory.os(name).getOperatingSystemFamily();
		}
	}
}
