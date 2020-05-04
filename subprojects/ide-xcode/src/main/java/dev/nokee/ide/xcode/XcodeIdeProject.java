package dev.nokee.ide.xcode;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

/**
 * Represents a Xcode IDE project.
 *
 * <blockquote>
 *     <p>
 *         An Xcode project is a repository for all the files, resources, and information required to build one or more software products.
 *         A project contains all the elements used to build your products and maintains the relationships between those elements.
 *         It contains one or more targets, which specify how to build products.
 *         A project defines default build settings for all the targets in the project (each target can also specify its own build settings, which override the project build settings).
 *     </p>
 *     <span>—Xcode Projects Concept</span>
 * </blockquote>
 *
 * It is assumed that each project will delegate to Gradle for building.
 * The generated Xcode projects act as a bridge to allow a native Xcode IDE experience.
 *
 * @since 0.3
 * @see <a href="https://developer.apple.com/library/content/featuredarticles/XcodeConcepts/Concept-Projects.html">Xcode Projects Concept</a>
 */
public interface XcodeIdeProject extends Named {
	/**
	 * Returns the location of the generated project.
	 * It defaults to <pre>${project.projectDir}/${project.name}.xcodeproj</pre>.
	 *
	 * @return a provider to the location of the generated project.
	 */
	Provider<FileSystemLocation> getLocation();

	/**
	 * Returns the targets for this project.
	 *
	 * @return a container of {@link XcodeIdeTarget} to configure the target, never null.
	 */
	NamedDomainObjectContainer<XcodeIdeTarget> getTargets();

	/**
	 * Configures the target container with the specified action.
	 *
	 * @param action a configuration action for the container of {@link XcodeIdeTarget} instances.
	 * @throws NullPointerException if the action is null
	 */
	void targets(Action<? super NamedDomainObjectContainer<XcodeIdeTarget>> action);
}
