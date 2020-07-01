package dev.nokee.ide.visualstudio;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;

/**
 * The configuration for mapping a Gradle project to Visual Studio projects.
 *
 * The project extension is register to sub-projects where the Visual Studio IDE plugin is applied.
 * The workspace extension, that is {@link VisualStudioIdeWorkspaceExtension}, is register to the root project instead.
 *
 * @since 0.5
 */
public interface VisualStudioIdeProjectExtension {
	/**
	 * Returns the projects to generate for this Gradle project.
	 *
	 * @return a container of {@link VisualStudioIdeProject} to configure the projects to create, never null.
	 */
	NamedDomainObjectContainer<VisualStudioIdeProject> getProjects();

	/**
	 * Configures the project container with the specified action.
	 *
	 * @param action a configuration action for the container of {@link VisualStudioIdeProject} instances.
	 * @throws NullPointerException if the action is null
	 */
	void projects(Action<? super NamedDomainObjectContainer<VisualStudioIdeProject>> action);
}
