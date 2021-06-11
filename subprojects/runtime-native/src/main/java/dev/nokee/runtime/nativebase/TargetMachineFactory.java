package dev.nokee.runtime.nativebase;

/**
 * A factory for creating {@link TargetMachine} instances.
 *
 * @since 0.1
 */
public interface TargetMachineFactory extends dev.nokee.platform.nativebase.TargetMachineFactory {
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
