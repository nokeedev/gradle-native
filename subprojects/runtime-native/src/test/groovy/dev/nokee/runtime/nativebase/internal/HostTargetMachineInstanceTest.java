package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
}
