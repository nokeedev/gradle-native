package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.core.CoordinateAxis;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

import java.util.Objects;

/**
 * Represents a target architecture of a configuration.
 * Typical architectures include "x86" and "x86-64".
 *
 * <p><b>Note:</b> This interface is not intended for implementation by build script or plugin authors.</p>
 *
 * @since 0.1
 */
public abstract class MachineArchitecture implements Named, dev.nokee.platform.nativebase.MachineArchitecture {
	public static final Attribute<MachineArchitecture> ARCHITECTURE_ATTRIBUTE = Attribute.of("dev.nokee.architecture", MachineArchitecture.class);

	public static final CoordinateAxis<MachineArchitecture> ARCHITECTURE_COORDINATE_AXIS = CoordinateAxis.of(MachineArchitecture.class, "machine-architecture");

	/**
	 * The intel x86 32-bit architecture canonical name.
	 */
	public static final String X86 = "x86";

	/**
	 * The intel x86 64-bit architecture canonical name.
	 */
	public static final String X86_64 = "x86-64";

	/**
	 * Creates a machine architecture using the canonical name of the specified architecture name.
	 *
	 * @param name an architecture name, must not be null
	 * @return a machine architecture, never null
	 * @since 0.5
	 */
	public static MachineArchitecture forName(String name) {
		return KnownMachineArchitectures.forName(name);
	}

	/**
	 * Returns the canonical name for this machine architecture.
	 *
	 * @return the canonical name, never null
	 * @since 0.5
	 */
	public String getCanonicalName() {
		return KnownMachineArchitectures.canonical(getName());
	}

	/**
	 * Returns whether or not the architecture has 32-bit pointer size.
	 *
	 * @return {@code true} if the architecture is 32-bit or {@code false} otherwise.
	 * @since 0.2
	 */
	public boolean is32Bit() {
		return KnownMachineArchitectures.is32BitArchitecture(getName());
	}

	/**
	 * Returns whether or not the architecture has 64-bit pointer size.
	 *
	 * @return {@code true} if the architecture is 64-bit or {@code false} otherwise.
	 * @since 0.2
	 */
	public boolean is64Bit() {
		return KnownMachineArchitectures.is64BitArchitecture(getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof MachineArchitecture)) {
			return false;
		}

		MachineArchitecture lhs = (MachineArchitecture) obj;
		return Objects.equals(getCanonicalName(), lhs.getCanonicalName());
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
