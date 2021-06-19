package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.TargetMachineBuilder;
import dev.nokee.runtime.nativebase.TargetMachineFactory;
import lombok.NonNull;

import static dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily.*;

public class DefaultTargetMachineFactory implements TargetMachineFactory {
	public static final DefaultTargetMachineFactory INSTANCE = new DefaultTargetMachineFactory();
	private static final MachineArchitecture HOST = MachineArchitecture.forName(System.getProperty("os.arch"));

	@Override
	public TargetMachineBuilder getWindows() {
		return new DefaultTargetMachineBuilder(WINDOWS, HOST);
	}

	@Override
	public TargetMachineBuilder getLinux() {
		return new DefaultTargetMachineBuilder(LINUX, HOST);
	}

	@Override
	public TargetMachineBuilder getMacOS() {
		return new DefaultTargetMachineBuilder(MACOS, HOST);
	}

	@Override
	public TargetMachineBuilder getFreeBSD() {
		return new DefaultTargetMachineBuilder(FREE_BSD, HOST);
	}

	/**
	 * Creates an {@link TargetMachine} with the host's operating system family and architecture.
	 *
	 * @return the {@link TargetMachine} for the host, never null.
	 */
	public static DefaultTargetMachine host() {
		return new DefaultTargetMachine(DefaultOperatingSystemFamily.HOST, HOST);
	}

	/**
	 * Creates an target machine with whatever operating system string will be passed.
	 * This is an escape hatch and should not be used.
	 *
	 * @param name a operating system family name, no validation is done on the name
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null.
	 */
	public DefaultTargetMachineBuilder os(String name) {
		return new DefaultTargetMachineBuilder(DefaultOperatingSystemFamily.forName(name), HOST);
	}

	public static class DefaultTargetMachineBuilder extends DefaultTargetMachine implements TargetMachineBuilder {
		private static final MachineArchitecture X86 = MachineArchitecture.forName(MachineArchitecture.X86);
		private static final MachineArchitecture X86_64 = MachineArchitecture.forName(MachineArchitecture.X86_64);

		public DefaultTargetMachineBuilder(@NonNull DefaultOperatingSystemFamily operatingSystemFamily, MachineArchitecture architecture) {
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
