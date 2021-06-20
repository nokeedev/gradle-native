package dev.nokee.runtime.nativebase;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.MachineArchitectureConfigurer;
import dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils.OperatingSystemFamilyConfigurer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.runtime.base.internal.ProvideAttributes.attributesOf;
import static dev.nokee.runtime.nativebase.MachineArchitecture.ARCHITECTURE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE;
import static dev.nokee.utils.ConfigurationUtils.asConsumable;
import static dev.nokee.utils.ConfigurationUtils.asResolvable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
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

	@ParameterizedTest(name = "provides attributes for consumable configuration {arguments}")
	@MethodSource("dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils#configurers")
	default void providesAttributesForConsumableConfiguration(OperatingSystemFamilyConfigurer os, MachineArchitectureConfigurer arch) {
		val machine = arch.apply(os.apply(createSubject()));
		val project = rootProject();
		val test = project.getConfigurations().create("test",
			asConsumable().andThen(attributesOf(machine)));

		assertAll(
			() -> assertThat(test, attributes(hasEntry(equalTo(OPERATING_SYSTEM_ATTRIBUTE), named(os.getName())))),
			() -> assertThat(test, attributes(hasEntry(equalTo(ARCHITECTURE_ATTRIBUTE), named(arch.getName())))),
			() -> assertThat(test, attributes(hasEntry(equalTo(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE), named(os.getName())))),
			() -> assertThat(test, attributes(hasEntry(equalTo(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE), named(arch.getName()))))
		);
	}

	@ParameterizedTest(name = "provides attributes for resolvable configuration {arguments}")
	@MethodSource("dev.nokee.runtime.nativebase.TargetMachineFactoryTestUtils#configurers")
	default void providesAttributesForResolvableConfiguration(OperatingSystemFamilyConfigurer os, MachineArchitectureConfigurer arch) {
		val machine = arch.apply(os.apply(createSubject()));
		val project = rootProject();
		val test = project.getConfigurations().create("test",
			asResolvable().andThen(attributesOf(machine)));

		assertAll(
			() -> assertThat(test, attributes(hasEntry(equalTo(OPERATING_SYSTEM_ATTRIBUTE), named(os.getName())))),
			() -> assertThat(test, attributes(hasEntry(equalTo(ARCHITECTURE_ATTRIBUTE), named(arch.getName())))),
			() -> assertThat(test, attributes(hasEntry(equalTo(org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE), named(os.getName())))),
			() -> assertThat(test, attributes(hasEntry(equalTo(org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE), named(arch.getName()))))
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
