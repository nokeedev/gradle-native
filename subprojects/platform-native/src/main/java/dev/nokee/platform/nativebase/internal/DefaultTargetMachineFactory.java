package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.TargetMachineBuilder;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.NonNull;

import static dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture.HOST;
import static dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture.*;
import static dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily.*;

public class DefaultTargetMachineFactory implements TargetMachineFactory {
	public static final DefaultTargetMachineFactory INSTANCE = new DefaultTargetMachineFactory();

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
	public TargetMachineBuilder os(String name) {
		return new DefaultTargetMachineBuilder(new DefaultOperatingSystemFamily(name), HOST);
	}

	private static class DefaultTargetMachineBuilder extends DefaultTargetMachine implements TargetMachineBuilder {
		public DefaultTargetMachineBuilder(@NonNull DefaultOperatingSystemFamily operatingSystemFamily, @NonNull DefaultMachineArchitecture architecture) {
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
			return new DefaultTargetMachine(getOperatingSystemFamily(), new UnknownMachineArchitecture(name));
		}
	}
}
