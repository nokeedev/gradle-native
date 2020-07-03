package dev.nokee.ide.visualstudio;

import org.gradle.api.provider.Provider;

import java.util.Map;

/**
 * Represent property group of a project configuration.
 *
 * It is assumed that Visual Studio will be delegating to Gradle for most of the build requirements.
 *
 * @since 0.5
 */
public interface VisualStudioIdePropertyGroup {
	/**
	 * Returns the elements of the property group.
	 * Each element is a key/value pair representing the properties.
	 *
	 * @return a provider of all the properties, never null.
	 */
	Provider<Map<String, Object>> getElements();

	/**
	 * Puts a new property entry for the specified name and value in the group.
	 * Number values, that is integer, long and double, are treated as number, Boolean values are treated as true/false string, any other value type will be converted to String using {@link Object#toString()} when resolved.
	 *
	 * @param name the name of the property to put, must not be null.
	 * @param value the value provider of the property to put, must not be null.
	 * @return this instance for chaining multiple configuration together.
	 */
	VisualStudioIdePropertyGroup put(String name, Provider<Object> value);

	/**
	 * Puts a new property entry for the specified name and value.
	 * Number values, that is integer, long and double, are treated as number, Boolean values are treated as true/false string, any other value type will be converted to String using {@link Object#toString()} when resolved.
	 *
	 * @param name the name of the property to put, must not be null.
	 * @param value the value of the property to put, must not be null.
	 * @return this instance for chaining multiple configuration together.
	 */
	VisualStudioIdePropertyGroup put(String name, Object value);
}
