package dev.nokee.ide.xcode;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;

/**
 * Represents a Xcode IDE target.
 *
 * <blockquote>
 *     <p>
 *         A target specifies a product to build and contains the instructions for building the product from a set of files in a project or workspace.
 *         A target defines a single product; it organizes the inputs into the build system—the source files and instructions for processing those source files—required to build that product.
 *         Projects can contain one or more targets, each of which produces one product.
 *     </p>
 *     <span>—Xcode Targets Concept</span>
 * </blockquote>
 *
 * @since 0.3
 * @see <a href="https://developer.apple.com/library/content/featuredarticles/XcodeConcepts/Concept-Targets.html">Xcode Targets Concept</a>
 */
public interface XcodeIdeTarget extends Named {
	/**
	 * Returns the product name this target is building.
	 * Typically, the product name is the base name of the produce reference.
	 *
	 * @return a property to configure the target's product name, never null.
	 */
	Property<String> getProductName();

	/**
	 * Returns the product type this target is building.
	 *
	 * Instances of {@link XcodeIdeProductType} are available via {@link XcodeIdeProductTypes} constants or via the {@link XcodeIdeProjectExtension#getProductTypes()} factory.
	 *
	 * @return a property to configure the target's product type, never null.
	 * @see XcodeIdeProductType
	 * @see XcodeIdeProductTypes
	 */
	Property<XcodeIdeProductType> getProductType();

	/**
	 * Returns the product reference filename of the product.
	 * It reference refers to the file name and extension of the built product.
	 *
	 * @return a property to configure the target's product filename.
	 */
	Property<String> getProductReference();

	/**
	 * Returns the build configurations this target can build.
	 *
	 * @return a container of {@link XcodeIdeBuildConfiguration} to configure the build configuration for this target, never null.
	 */
	NamedDomainObjectContainer<XcodeIdeBuildConfiguration> getBuildConfigurations();

	/**
	 * Configures the build configuration container with the specified action.
	 *
	 * @param action a configuration action for the container of {@link XcodeIdeBuildConfiguration} instances.
	 * @throws NullPointerException if the action is null
	 */
	void buildConfigurations(Action<? super NamedDomainObjectContainer<XcodeIdeBuildConfiguration>> action);

	/**
	 * Returns the sources this target will build.
	 *
	 * @return a file collection to configure the sources for this target, never null.
	 */
	ConfigurableFileCollection getSources();

	/**
	 * Returns this Xcode target instance.
	 * This method is offered for convenience when configuring Xcode IDE via the build DSL.
	 *
	 * @return this {@link XcodeIdeTarget} instance, never null.
	 */
	XcodeIdeTarget getIdeTarget();
}
