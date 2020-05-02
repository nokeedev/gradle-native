package dev.nokee.ide.xcode;

import org.gradle.api.Named;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Property;

/**
 * Represent a build configuration of a target.
 *
 * @since 0.3
 */
public interface XcodeIdeBuildConfiguration extends Named {
	/**
	 * Returns the build settings for this build configuration.
	 *
	 * @return a {@link XcodeIdeBuildSettings} instance, never null.
	 */
	XcodeIdeBuildSettings getBuildSettings();

	/**
	 * Returns the product location built by Gradle.
	 *
	 * @return a property to configure the product location built by Gradle.
	 */
	Property<FileSystemLocation> getProductLocation();
}
