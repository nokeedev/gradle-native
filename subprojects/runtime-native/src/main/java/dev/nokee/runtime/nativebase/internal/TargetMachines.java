package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateTuple;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import static java.lang.System.getProperty;

public final class TargetMachines {
	private TargetMachines() {}

	private static final NamedTargetMachine HOST = new DefaultTargetMachine("host", OperatingSystemFamily.forName(getProperty("os.name")), MachineArchitecture.forName(getProperty("os.arch")));

	/**
	 * Creates an {@link TargetMachine} with the host's operating system family and architecture.
	 *
	 * @return the {@link TargetMachine} for the host, never null.
	 */
	public static NamedTargetMachine host() {
		return HOST;
	}

	public static boolean isTargetingHost(TargetMachine targetMachine) {
		return HOST.getOperatingSystemFamily().equals(targetMachine.getOperatingSystemFamily())
			&& HOST.getArchitecture().equals(targetMachine.getArchitecture());
	}

	public static NamedTargetMachine of(String machine) {
		val machineTuple = machine.split("-");
		return new DefaultTargetMachine(machineTuple[0] + StringUtils.capitalize(machineTuple[1]), OperatingSystemFamily.named(machineTuple[0]), MachineArchitecture.named(machineTuple[1]));
	}

	// Declare adhoc target machine to help internal APIs
	public interface NamedTargetMachine extends TargetMachine, Coordinate<TargetMachine>, CoordinateTuple {
		NamedTargetMachine named(String name);
	}

	@EqualsAndHashCode(callSuper = true)
	private static final class DefaultTargetMachine extends AbstractTargetMachine implements NamedTargetMachine {
		private final String name;

		DefaultTargetMachine(String name, OperatingSystemFamily operatingSystemFamily, MachineArchitecture architecture) {
			super(operatingSystemFamily, architecture);
			this.name = name;
		}

		@Override
		public NamedTargetMachine named(String name) {
			return new DefaultTargetMachine(name, getOperatingSystemFamily(), getArchitecture());
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
