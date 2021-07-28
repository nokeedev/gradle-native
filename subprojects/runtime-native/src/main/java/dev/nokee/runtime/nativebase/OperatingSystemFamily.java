package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.core.CoordinateAxis;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

import java.util.Objects;

/**
 * Represents the operating system family of a configuration.
 * Typical operating system family include Windows, Linux, and macOS.
 *
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors.</p>
 *
 * @since 0.1
 */
public abstract class OperatingSystemFamily implements Named, dev.nokee.platform.nativebase.OperatingSystemFamily {
	/**
	 * The operating system attribute for dependency resolution.
	 * @since 0.5
	 */
	public static final Attribute<OperatingSystemFamily> OPERATING_SYSTEM_ATTRIBUTE = Attribute.of("dev.nokee.operatingSystem", OperatingSystemFamily.class);

	/**
	 * The operating system coordinate axis for variant calculation.
	 * @since 0.5
	 */
	public static final CoordinateAxis<OperatingSystemFamily> OPERATING_SYSTEM_COORDINATE_AXIS = CoordinateAxis.of(OperatingSystemFamily.class);

	/**
	 * Creates a operating system family using the canonical name of the specified operating system family name.
	 *
	 * @param name an operating system family name, must not be null
	 * @return a operating system family, never null
	 * @since 0.5
	 */
	public static OperatingSystemFamily forName(String name) {
		return KnownOperatingSystemFamilies.forName(name);
	}

	public static OperatingSystemFamily named(String name) {
		return new DefaultOperatingSystemFamily(name);
	}

	public abstract String getName();

	public String getCanonicalName() {
		return KnownOperatingSystemFamilies.canonical(getName());
	}

	/**
	 * The Windows operating system family.
	 * @since 0.5
	 */
	public static final String WINDOWS = "windows";

	/**
	 * Checks if the operating system family is Windows.
	 *
	 * @return {@code true} if the operating system family is Windows or {@code false} otherwise.
	 */
	public boolean isWindows() {
		return is(WINDOWS);
	}

	/**
	 * The Windows operating system family.
	 * @since 0.5
	 */
	public static final String LINUX = "linux";

	/**
	 * Checks if the operating system family is Linux.
	 *
	 * @return {@code true} if the operating system family is Linux or {@code false} otherwise.
	 */
	public boolean isLinux() {
		return is(LINUX);
	}

	/**
	 * The macOS operating system family.
	 * @since 0.5
	 */
	public static final String MACOS = "macos";

	/**
	 * Checks if the operating system family is macOS.
	 *
	 * @return {@code true} if the operating system family is macOS or {@code false} otherwise.
	 */
	public boolean isMacOs() {
		return is(MACOS);
	}

	/**
	 * Check if the operating system family is macOS.
	 *
	 * @return {@code true} if the operating system family is macOS or {@code false} otherwise.
	 * @since 0.2
	 */
	public final boolean isMacOS() {
		return isMacOs();
	}

	/**
	 * The FreeBSD operating system family.
	 * @since 0.5
	 */
	public static final String FREE_BSD = "freebsd";

	/**
	 * Checks if the operating system family is FreeBSD.
	 *
	 * @return {@code true} if the operating system family is FreeBSD or {@code false} otherwise.
	 * @since 0.2
	 */
	public boolean isFreeBSD() {
		return is(FREE_BSD);
	}

	/**
	 * The iOS operating system family.
	 * @since 0.5
	 */
	public static final String IOS = "ios";

	/**
	 * Check if the operating system family is iOS.
	 *
	 * @return {@code true} if the operating system family is iOS or {@code false} otherwise.
	 * @since 0.4
	 */
	public boolean isIos() {
		return is(IOS);
	}

	/**
	 * Check if the operating system family is iOS.
	 *
	 * @return {@code true} if the operating system family is iOS or {@code false} otherwise.
	 * @since 0.4
	 */
	public final boolean isiOS() {
		return isIos();
	}

	/**
	 * The Solaris operating system family.
	 * @since 0.5
	 */
	public static final String SOLARIS = "solaris";

	/**
	 * Check if the operating system family is Solaris.
	 *
	 * @return {@code true} if the operating system family is Solaris or {@code false} otherwise.
	 * @since 0.5
	 */
	public boolean isSolaris() {
		return is(SOLARIS);
	}

	/**
	 * The FreeBSD operating system family.
	 * @since 0.5
	 */
	public static final String HP_UX = "hpux";

	/**
	 * Check if the operating system family is Hewlett Packard Unix (e.g. HP-UX).
	 *
	 * @return {@code true} if the operating system family is HP-UX or {@code false} otherwise.
	 * @since 0.5
	 */
	public boolean isHewlettPackardUnix() {
		return is(HP_UX);
	}

	private boolean is(String osFamily) {
		return getCanonicalName().equals(osFamily);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof OperatingSystemFamily)) {
			return false;
		}

		OperatingSystemFamily other = (OperatingSystemFamily) obj;
		return Objects.equals(getCanonicalName(), other.getCanonicalName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getCanonicalName());
	}

	@Override
	public String toString() {
		return getName();
	}
}
