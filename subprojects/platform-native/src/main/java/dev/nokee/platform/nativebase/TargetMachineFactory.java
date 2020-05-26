package dev.nokee.platform.nativebase;

import dev.nokee.runtime.nativebase.TargetMachine;

/**
 * A factory for creating {@link TargetMachine} instances.
 *
 * @since 0.1
 */
public interface TargetMachineFactory {
	/**
	 * Creates a {@link TargetMachineBuilder} for the Windows operating system family and the architecture of the current host.
	 *
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null.
	 */
	TargetMachineBuilder getWindows();

	/**
	 * Creates a {@link TargetMachineBuilder} for the Linux operating system family and the architecture of the current host.
	 *
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null.
	 */
	TargetMachineBuilder getLinux();

	/**
	 * Creates a {@link TargetMachineBuilder} for the macOS operating system family and the architecture of the current host.
	 *
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null.
	 */
	TargetMachineBuilder getMacOS();

	/**
	 * Creates a {@link TargetMachineBuilder} for the FreeBSD operating system family and the architecture of the current host.
	 *
	 * @return a {@link TargetMachineBuilder} to further configure the target machine, never null.
	 */
	TargetMachineBuilder getFreeBSD();
}
