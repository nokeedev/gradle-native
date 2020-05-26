package dev.nokee.platform.nativebase;

/**
 * Represents the operating system family of a configuration.
 * Typical operating system family include Windows, Linux, and macOS.
 *
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors.</p>
 *
 * @since 0.1
 */
@Deprecated
public interface OperatingSystemFamily {
	/**
	 * Checks if the operating system family is Windows.
	 *
	 * @return {@code true} if the operating system family is Windows or {@code false} otherwise.
	 */
	boolean isWindows();

	/**
	 * Checks if the operating system family is Linux.
	 *
	 * @return {@code true} if the operating system family is Linux or {@code false} otherwise.
	 */
	boolean isLinux();

	/**
	 * Checks if the operating system family is macOS.
	 *
	 * @return {@code true} if the operating system family is macOS or {@code false} otherwise.
	 */
	boolean isMacOs();

	/**
	 * Check if the operating system family is macOS.
	 *
	 * @return {@code true} if the operating system family is macOS or {@code false} otherwise.
	 * @since 0.2
	 */
	default boolean isMacOS() {
		return isMacOs();
	}

	/**
	 * Checks if the operating system family is FreeBSD.
	 *
	 * @return {@code true} if the operating system family is FreeBSD or {@code false} otherwise.
	 * @since 0.2
	 */
	boolean isFreeBSD();
}
