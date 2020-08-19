package dev.nokee.ide.visualstudio;

import dev.nokee.ide.base.IdeProjectReference;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.util.Set;

/**
 * A reference to an Visual Studio IDE project.
 *
 * @since 0.5
 * @see IdeProjectReference for more information.
 */
public interface VisualStudioIdeProjectReference extends IdeProjectReference {
	/**
	 * Returns the project location, that is the location of the .vcxproj, for this project reference.
	 *
	 * @return a provider of this project reference's vcxproj, never null.
	 */
	Provider<FileSystemLocation> getProjectLocation();

	/**
	 * Returns the project global universal identifier of this project reference.
	 *
	 * @return a provider of {@link VisualStudioIdeGuid} of this project reference, never null.
	 */
	Provider<VisualStudioIdeGuid> getProjectGuid();

	/**
	 * Returns the project configuration of this project reference.
	 *
	 * @return a provider of {@link VisualStudioIdeProjectConfiguration} of this project reference, never null.
	 */
	Provider<Set<VisualStudioIdeProjectConfiguration>> getProjectConfigurations();
}
