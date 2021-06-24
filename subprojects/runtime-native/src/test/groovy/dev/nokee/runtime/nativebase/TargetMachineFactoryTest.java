package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import org.junit.jupiter.api.Nested;

class TargetMachineFactoryTest implements TargetMachineFactoryTester{
	@Override
	public TargetMachineFactory createSubject() {
		return NativeRuntimePlugin.TARGET_MACHINE_FACTORY;
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
