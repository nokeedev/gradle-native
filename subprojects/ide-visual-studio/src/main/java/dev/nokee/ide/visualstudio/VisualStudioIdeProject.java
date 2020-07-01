package dev.nokee.ide.visualstudio;

import dev.nokee.ide.base.IdeProject;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

/**
 * @since 0.5
 */
public interface VisualStudioIdeProject extends IdeProject {
	/**
	 * Returns the location of the generated project.
	 * It defaults to <pre>${project.projectDir}/${project.name}.xcodeproj</pre>.
	 *
	 * @return a provider to the location of the generated project.
	 */
	Provider<FileSystemLocation> getLocation();
}
