package dev.nokee.ide.visualstudio;

/**
 * The configuration for mapping a Gradle project to Visual Studio projects and solution.
 *
 * The workspace extension is register to the {@link org.gradle.api.Project} instance only for the root project where the Visual Studio IDE plugin is applied.
 * The project extension, that is {@link VisualStudioIdeProjectExtension}, is register to sub-projects instead.
 *
 * @since 0.5
 */
public interface VisualStudioIdeWorkspaceExtension extends VisualStudioIdeProjectExtension {
	/**
	 * Returns the generated Visual Studio solution for this Gradle build.
	 *
	 * @return a {@link VisualStudioIdeSolution} instance, never null.
	 */
	VisualStudioIdeSolution getSolution();
}
