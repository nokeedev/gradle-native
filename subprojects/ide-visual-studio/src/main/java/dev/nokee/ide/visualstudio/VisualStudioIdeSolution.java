package dev.nokee.ide.visualstudio;

import dev.nokee.ide.base.IdeWorkspace;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

/**
 * Represents the generated Visual Studio IDE solution.
 *
 * @since 0.5
 */
public interface VisualStudioIdeSolution extends IdeWorkspace<VisualStudioIdeProjectReference> {
	/**
	 * Returns Visual Studio projects to include in the solution.
	 *
	 * @return a property to configure the projects to include in the solution.
	 */
	SetProperty<VisualStudioIdeProjectReference> getProjects();

	/**
	 * Returns the location of the generated solution.
	 * It defaults to <pre>${project.projectDir}/${project.name}.sln</pre>.
	 *
	 * @return a provider to the location of the generated workspace.
	 */
	Provider<FileSystemLocation> getLocation();
}
