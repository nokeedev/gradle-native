package dev.nokee.runtime.nativebase;

/**
 * A factory for creating {@link TargetBuildType} instances.
 *
 * @since 0.5
 */
public interface TargetBuildTypeFactory extends dev.nokee.platform.nativebase.TargetBuildTypeFactory {

	/**
	 * Creates a build type of the specified name.
	 *
	 * @param name  the name of the build type to create, must not be null
	 * @return a {@link TargetBuildType} instance representing a build type with the specified name, never null.
	 */
	TargetBuildType named(String name);
}
