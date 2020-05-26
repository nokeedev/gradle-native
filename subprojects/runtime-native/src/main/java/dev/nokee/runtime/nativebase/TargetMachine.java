package dev.nokee.runtime.nativebase;

/**
 * Represents a combination of operating system and cpu architecture that a variant might be built for.
 *
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors.
 * Use {@link dev.nokee.platform.nativebase.TargetMachineFactory} to create an instance.
 * </p>
 *
 * @since 0.1
 */
public interface TargetMachine extends dev.nokee.platform.nativebase.TargetMachine {
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
