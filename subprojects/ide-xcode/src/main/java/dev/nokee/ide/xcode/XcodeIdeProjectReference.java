package dev.nokee.ide.xcode;

import dev.nokee.ide.base.IdeProjectReference;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

/**
 * A reference to an Xcode IDE project.
 *
 * @since 0.5
 * @see IdeProjectReference for more information.
 */
public interface XcodeIdeProjectReference extends IdeProjectReference {
	/**
	 * Returns the location of the Xcode IDE project for this reference.
	 *
	 * @return a provider for the Xcode IDE project location, never null.
	 */
	Provider<FileSystemLocation> getLocation();
}
