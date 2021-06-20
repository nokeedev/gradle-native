package dev.nokee.runtime.nativebase;

/**
 * A builder for configuring the architecture of a {@link TargetMachine} instances.
 *
 * @since 0.1
 */
public interface TargetMachineBuilder extends TargetMachine, dev.nokee.platform.nativebase.TargetMachineBuilder {
	/**
	 * Creates a {@link TargetMachine} for the operating system of this instance and the x86 32-bit architecture.
	 *
	 * @return a {@link TargetMachine} instance, never null.
	 */
	TargetMachine getX86();

	/**
	 * Creates a {@link TargetMachine} for the operating system of this instance and the x86 64-bit architecture.
	 *
	 * @return a {@link TargetMachine} instance, never null.
	 */
	TargetMachine getX86_64();

	/**
	 * Creates a target machine for the operating system of this instance and with whatever architecture string will be passed.
	 *
	 * @param architectureName  an architecture name, must not be null
	 * @return a {@link TargetMachine} instance, never null
	 * @since 0.5
	 */
	TargetMachine architecture(String architectureName);
}
