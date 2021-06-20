package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.core.CoordinateAxis;

/**
 * Represents a combination of operating system and cpu architecture that a variant might be built for.
 *
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors.
 * Use {@link TargetMachineFactory} to create an instance.
 * </p>
 *
 * @since 0.1
 */
public interface TargetMachine extends dev.nokee.platform.nativebase.TargetMachine {
	/**
	 * The target machine coordinate axis for variant calculation.
	 * @since 0.5
	 */
	CoordinateAxis<TargetMachine> TARGET_MACHINE_COORDINATE_AXIS = CoordinateAxis.of(TargetMachine.class, "target-machine");

	/**
	 * Returns the target operating system family.
	 *
	 * @return a {@link OperatingSystemFamily} instance, never null.
	 */
	OperatingSystemFamily getOperatingSystemFamily();

	/**
	 * Returns the target architecture.
	 *
	 * @return a {@link MachineArchitecture} instance, never null.
	 */
	MachineArchitecture getArchitecture();
}
