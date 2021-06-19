package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.*;

public class DefaultTargetMachineFactory implements TargetMachineFactory {
	public static final DefaultTargetMachineFactory INSTANCE = new DefaultTargetMachineFactory();
	private static final MachineArchitecture HOST_ARCH = MachineArchitecture.forName(System.getProperty("os.arch"));
	private static final OperatingSystemFamily HOST_OS = OperatingSystemFamily.forName(System.getProperty("os.name"));
	private static final OperatingSystemFamily WINDOWS = OperatingSystemFamily.forName(OperatingSystemFamily.WINDOWS);
	private static final OperatingSystemFamily LINUX = OperatingSystemFamily.forName(OperatingSystemFamily.LINUX);
	private static final OperatingSystemFamily MACOS = OperatingSystemFamily.forName(OperatingSystemFamily.MACOS);
	private static final OperatingSystemFamily FREE_BSD = OperatingSystemFamily.forName(OperatingSystemFamily.FREE_BSD);

	@Override
	public TargetMachineBuilder getWindows() {
		return new DefaultTargetMachineBuilder(WINDOWS, HOST_ARCH);
	}

	@Override
	public TargetMachineBuilder getLinux() {
		return new DefaultTargetMachineBuilder(LINUX, HOST_ARCH);
	}

	@Override
	public TargetMachineBuilder getMacOS() {
		return new DefaultTargetMachineBuilder(MACOS, HOST_ARCH);
	}

	@Override
	public TargetMachineBuilder getFreeBSD() {
		return new DefaultTargetMachineBuilder(FREE_BSD, HOST_ARCH);
	}

	/**
	 * Creates an {@link TargetMachine} with the host's operating system family and architecture.
	 *
	 * @return the {@link TargetMachine} for the host, never null.
	 */
	public static DefaultTargetMachine host() {
		return new DefaultTargetMachine(HOST_OS, HOST_ARCH);
	}

	/**
	 * Creates an target machine with whatever operating system string will be passed.
	 * This is an escape hatch and should not be used.
	 *
	 * @param name a operating system family name, no validation is done on the name
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null.
	 */
	public DefaultTargetMachineBuilder os(String name) {
		return new DefaultTargetMachineBuilder(OperatingSystemFamily.forName(name), HOST_ARCH);
	}

	public static class DefaultTargetMachineBuilder extends DefaultTargetMachine implements TargetMachineBuilder {
		private static final MachineArchitecture X86 = MachineArchitecture.forName(MachineArchitecture.X86);
		private static final MachineArchitecture X86_64 = MachineArchitecture.forName(MachineArchitecture.X86_64);

		public DefaultTargetMachineBuilder(OperatingSystemFamily operatingSystemFamily, MachineArchitecture architecture) {
			super(operatingSystemFamily, architecture);
		}

		@Override
		public TargetMachine getX86() {
			return new DefaultTargetMachine(getOperatingSystemFamily(), X86);
		}

		@Override
		public TargetMachine getX86_64() {
			return new DefaultTargetMachine(getOperatingSystemFamily(), X86_64);
		}

		public TargetMachine architecture(String name) {
			return new DefaultTargetMachine(getOperatingSystemFamily(), MachineArchitecture.forName(name));
		}
	}
}
