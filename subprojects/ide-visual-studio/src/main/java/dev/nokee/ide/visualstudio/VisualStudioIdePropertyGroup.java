/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
