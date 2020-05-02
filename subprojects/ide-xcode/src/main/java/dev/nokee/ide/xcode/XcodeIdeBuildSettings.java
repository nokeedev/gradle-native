package dev.nokee.ide.xcode;

import org.gradle.api.provider.Provider;

import java.util.Map;

/**
 * Represent the build settings of a build configuration for a target.
 *
 * <blockquote>
 *     <p>
 *         A build setting is a variable that contains information about how a particular aspect of a product’s build process should be performed.
 *         For example, the information in a build setting can specify which options Xcode passes to the compiler.
 *     </p>
 *     <span>—Xcode Build Settings Concept</span>
 * </blockquote>
 *
 * It is assumed that Xcode will be delegating to Gradle for most of the build requirements.
 *
 * @since 0.3
 * @see <a href="https://developer.apple.com/library/content/featuredarticles/XcodeConcepts/Concept-Build_Settings.html">Xcode Build Settings Concept</a>
 */
public interface XcodeIdeBuildSettings {
	/**
	 * Returns the elements of the build settings.
	 * Each element is a key/value pair representing the build setting entries.
	 *
	 * @return a provider of all the build settings, never null.
	 */
	Provider<Map<String, Object>> getElements();

	/**
	 * Puts a new build setting entry for the specified name and value.
	 * Number values, that is integer, long and double, are treated as number, any other value type will be converted to String using {@link Object#toString()} when resolved.
	 *
	 * @param name the name of the build setting to put, must not be null.
	 * @param value the value provider of the build setting to put, must not be null.
	 * @return this instance for chaining multiple configuration together.
	 */
	XcodeIdeBuildSettings put(String name, Provider<Object> value);

	/**
	 * Puts a new build setting entry for the specified name and value.
	 * Number values, that is integer, long and double, are treated as number, any other value type will be converted to String using {@link Object#toString()} when resolved.
	 *
	 * @param name the name of the build setting to put, must not be null.
	 * @param value the value of the build setting to put, must not be null.
	 * @return this instance for chaining multiple configuration together.
	 */
	XcodeIdeBuildSettings put(String name, Object value);
}
