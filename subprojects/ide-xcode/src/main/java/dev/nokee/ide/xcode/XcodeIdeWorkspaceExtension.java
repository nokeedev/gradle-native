package dev.nokee.ide.xcode;

/**
 * The configuration for mapping a Gradle project to Xcode projects and workspace.
 *
 * The workspace extension is register to the {@link org.gradle.api.Project} instance only for the root project where the Xcode IDE plugin is applied.
 * The project extension, that is {@link XcodeIdeProjectExtension}, is register to sub-projects instead.
 *
 * @since 0.3
 */
public interface XcodeIdeWorkspaceExtension extends XcodeIdeProjectExtension {
	/**
	 * Returns the generated Xcode workspace for this Gradle build.
	 *
	 * @return a {@link XcodeIdeWorkspace} instance, never null.
	 */
	XcodeIdeWorkspace getWorkspace();
}
