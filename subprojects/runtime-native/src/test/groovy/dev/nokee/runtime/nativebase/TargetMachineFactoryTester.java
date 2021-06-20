package dev.nokee.runtime.nativebase;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.MachineArchitectureConfigurer;
import dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.OperatingSystemFamilyConfigurer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertAll;

public interface TargetMachineFactoryTester {
	TargetMachineFactory createSubject();

	@ParameterizedTest(name = "can create machine targeting {arguments}")
	@MethodSource("dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils#configurers")
	default void canCreateMachineTargetingOperatingSystemAndArchitecture(OperatingSystemFamilyConfigurer os, MachineArchitectureConfigurer arch) {
		val machine = arch.apply(os.apply(createSubject()));
		assertAll(
			() -> os.assertOperatingSystem(machine),
			() -> arch.assertMachineArchitecture(machine)
		);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		val factory = createSubject();
		new EqualsTester()
			.addEqualityGroup(factory.getWindows(), factory.getWindows())
			.addEqualityGroup(factory.getLinux().getX86(), factory.getLinux().getX86())
			.addEqualityGroup(factory.getMacOS().getX86_64(), factory.getMacOS().getX86_64())
			.addEqualityGroup(factory.getFreeBSD())
			.addEqualityGroup(factory.os("some-os"), factory.os("some-os"))
			.addEqualityGroup(factory.os("some-os").architecture("some-arch"))
			.testEquals();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(createSubject());
		new NullPointerTester().testAllPublicInstanceMethods(createSubject().os("some-os"));
	}
}
