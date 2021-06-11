package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.CommonMachineArchitectureTester;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

class DefaultMachineArchitectureTest implements CommonMachineArchitectureTester {
	@Test
	void canCompareMachineArchitectureInstance() {
		assertThat(DefaultMachineArchitecture.X86, equalTo(DefaultMachineArchitecture.X86));
		assertThat(DefaultMachineArchitecture.X86, not(equalTo(DefaultMachineArchitecture.X86_64)));
	}

	@Test
	void defaultsToTheRightPreMadeInstances() {
		// TODO Improve when we test on other architectures
		assertThat(DefaultMachineArchitecture.HOST, equalTo(DefaultMachineArchitecture.X86_64));
	}

	@Override
	public MachineArchitecture createSubject(String name) {
		return DefaultMachineArchitecture.forName(name);
	}
}
