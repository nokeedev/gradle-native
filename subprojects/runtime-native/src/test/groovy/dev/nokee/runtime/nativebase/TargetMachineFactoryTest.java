package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.nativebase.internal.RuntimeNativePlugin;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TargetMachineFactoryTest implements TargetMachineFactoryTester{
	@Override
	public TargetMachineFactory createSubject() {
		return RuntimeNativePlugin.TARGET_MACHINE_FACTORY;
	}

	@Nested
	class AdhocMachineArchitectureTest implements KnownMachineArchitectureTester, UnknownMachineArchitectureTester {
		@Override
		public MachineArchitecture createSubject(String name) {
			return TargetMachineFactoryTest.this.createSubject().os("some-os").architecture(name).getArchitecture();
		}
	}

	@Nested
	class AdhocOperatingSystemFamilyTest implements KnownOperatingSystemFamilyTester, UnknownOperatingSystemFamilyTester {
		@Override
		public OperatingSystemFamily createSubject(String name) {
			return TargetMachineFactoryTest.this.createSubject().os(name).getOperatingSystemFamily();
		}
	}
}
