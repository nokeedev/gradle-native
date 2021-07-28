package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateTuple;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;

import static java.lang.System.getProperty;

public final class TargetMachines {
	private TargetMachines() {}

	private static final HostTargetMachine HOST = new DefaultHostTargetMachine("host");

	/**
	 * Creates an {@link TargetMachine} with the host's operating system family and architecture.
	 *
	 * @return the {@link TargetMachine} for the host, never null.
	 */
	public static HostTargetMachine host() {
		return HOST;
	}

	public static boolean isTargetingHost(TargetMachine targetMachine) {
		return HOST.getOperatingSystemFamily().equals(targetMachine.getOperatingSystemFamily())
			&& HOST.getArchitecture().equals(targetMachine.getArchitecture());
	}

	// Declare host target machine to help internal APIs
	public interface HostTargetMachine extends TargetMachine, Coordinate<TargetMachine>, CoordinateTuple {
		HostTargetMachine named(String name);
	}

	/** @see #host() */
	private static final class DefaultHostTargetMachine extends AbstractTargetMachine implements HostTargetMachine {
		private final String name;

		DefaultHostTargetMachine(String name) {
			super(OperatingSystemFamily.forName(getProperty("os.name")), MachineArchitecture.forName(getProperty("os.arch")));
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public HostTargetMachine named(String name) {
			return new DefaultHostTargetMachine(name);
		}
	}
}
