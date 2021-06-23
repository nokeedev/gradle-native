package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.*;

final class DefaultTargetMachineFactory implements TargetMachineFactory {
	private static final MachineArchitecture HOST_ARCH = MachineArchitecture.forName(System.getProperty("os.arch"));
	private static final OperatingSystemFamily WINDOWS = OperatingSystemFamily.forName(OperatingSystemFamily.WINDOWS);
	private static final OperatingSystemFamily LINUX = OperatingSystemFamily.forName(OperatingSystemFamily.LINUX);
	private static final OperatingSystemFamily MACOS = OperatingSystemFamily.forName(OperatingSystemFamily.MACOS);
	private static final OperatingSystemFamily FREE_BSD = OperatingSystemFamily.forName(OperatingSystemFamily.FREE_BSD);

	@Override
	public TargetMachineBuilder getWindows() {
		return new DefaultTargetMachineBuilder(WINDOWS);
	}

	@Override
	public TargetMachineBuilder getLinux() {
		return new DefaultTargetMachineBuilder(LINUX);
	}

	@Override
	public TargetMachineBuilder getMacOS() {
		return new DefaultTargetMachineBuilder(MACOS);
	}

	@Override
	public TargetMachineBuilder getFreeBSD() {
		return new DefaultTargetMachineBuilder(FREE_BSD);
	}

	// Internal, exposed for convenience.
	// Host is just a placeholder, not a target machine.
	public TargetMachine getHost() {
		return TargetMachines.host();
	}

	@Override
	public DefaultTargetMachineBuilder os(String name) {
		return new DefaultTargetMachineBuilder(OperatingSystemFamily.forName(name));
	}

	private static final class DefaultTargetMachineBuilder extends AbstractTargetMachine implements TargetMachineBuilder {
		private static final MachineArchitecture X86 = MachineArchitecture.forName(MachineArchitecture.X86);
		private static final MachineArchitecture X86_64 = MachineArchitecture.forName(MachineArchitecture.X86_64);

		public DefaultTargetMachineBuilder(OperatingSystemFamily operatingSystemFamily) {
			super(operatingSystemFamily, HOST_ARCH);
		}

		@Override
		public TargetMachine getX86() {
			return new DefaultTargetMachine(getOperatingSystemFamily(), X86);
		}

		@Override
		public TargetMachine getX86_64() {
			return new DefaultTargetMachine(getOperatingSystemFamily(), X86_64);
		}

		@Override
		public TargetMachine architecture(String name) {
			return new DefaultTargetMachine(getOperatingSystemFamily(), MachineArchitecture.forName(name));
		}
	}

	private static final class DefaultTargetMachine extends AbstractTargetMachine {
		DefaultTargetMachine(OperatingSystemFamily operatingSystemFamily, MachineArchitecture architecture) {
			super(operatingSystemFamily, architecture);
		}
	}
}
