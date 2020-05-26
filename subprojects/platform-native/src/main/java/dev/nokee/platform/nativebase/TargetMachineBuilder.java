package dev.nokee.platform.nativebase;

import dev.nokee.runtime.nativebase.TargetMachine;

/**
 * A builder for configuring the architecture of a {@link TargetMachine} instances.
 *
 * @since 0.1
 */
public interface TargetMachineBuilder extends TargetMachine {
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
}
