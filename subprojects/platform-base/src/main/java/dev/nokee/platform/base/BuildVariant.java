package dev.nokee.platform.base;

/**
 * A representation of the selected values for each dimension for a build.
 *
 * @since 0.5
 */
public interface BuildVariant {
	/**
	 * Returns true if the build variant contains the specified axis value.
	 *
	 * @param axisValue a axis value to check against this build variant.
	 * @return {@code true} if the axis value is in the build type or {@code false} otherwise.
	 */
	boolean hasAxisOf(Object axisValue);
}
