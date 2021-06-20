package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;

import static java.lang.System.getProperty;

public final class TargetMachines {
	private TargetMachines() {}

	private static final TargetMachine HOST = new HostTargetMachine();

	/**
	 * Creates an {@link TargetMachine} with the host's operating system family and architecture.
	 *
	 * @return the {@link TargetMachine} for the host, never null.
	 */
	public static TargetMachine host() {
		return HOST;
	}

	public static boolean isTargetingHost(TargetMachine targetMachine) {
		return HOST.equals(targetMachine);
	}

	private static final class HostTargetMachine extends AbstractTargetMachine {
		HostTargetMachine() {
			super(OperatingSystemFamily.forName(getProperty("os.name")), MachineArchitecture.forName(getProperty("os.arch")));
		}
	}
}
