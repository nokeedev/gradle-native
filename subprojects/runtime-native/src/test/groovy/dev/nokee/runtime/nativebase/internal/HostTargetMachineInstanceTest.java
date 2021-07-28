package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinates;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class HostTargetMachineInstanceTest {
	@Test
	void hasHostOperatingSystemFamily() {
		assertThat(TargetMachines.host().getOperatingSystemFamily(),
			equalTo(OperatingSystemFamily.forName(System.getProperty("os.name"))));
	}

	@Test
	void hasHostMachineArchitecture() {
		assertThat(TargetMachines.host().getArchitecture(),
			equalTo(MachineArchitecture.forName(System.getProperty("os.arch"))));
	}

	@Test
	void canRenameHostTargetMachine() {
		val subject = TargetMachines.host().named("foo");
		assertAll(
			() -> assertThat(subject.getOperatingSystemFamily(), equalTo(OperatingSystemFamily.forName(System.getProperty("os.name")))),
			() -> assertThat(subject.getArchitecture(), equalTo(MachineArchitecture.forName(System.getProperty("os.arch")))),
			() -> assertThat(subject, hasToString("foo"))
		);
	}

	@Test
	void checkToString() {
		assertThat(TargetMachines.host(), hasToString("host"));
	}

	@Test
	void hasCoordinateAxisAndValue() {
		val subject = TargetMachines.host();
		assertAll(
			() -> assertThat(subject.getAxis(), is(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)),
			() -> assertThat(subject.getValue(), is(subject))
		);
	}

	@Test
	void hasOperatingSystemAndArchitectureCoordinate() {
		val subject = TargetMachines.host();
		assertThat(subject, contains(Coordinates.of(subject.getOperatingSystemFamily()), Coordinates.of(subject.getArchitecture())));
	}
}
