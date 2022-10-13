/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.xcode;

/**
 * Represent build settings for a specific invocation.
 */
public interface XCBuildSettings {
	/**
	 * Returns the evaluated build setting of the specified name.
	 *
	 * @param name  the name of the build setting to find and evaluate, must not be null
	 * @return the evaluated build setting value, never null
	 */
	String get(String name);
}
