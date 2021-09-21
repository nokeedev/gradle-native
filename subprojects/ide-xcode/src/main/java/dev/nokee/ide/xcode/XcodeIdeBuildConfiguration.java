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
package dev.nokee.ide.xcode;

import org.gradle.api.Named;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Property;

/**
 * Represent a build configuration of a target.
 *
 * @since 0.3
 */
public interface XcodeIdeBuildConfiguration extends Named {
	/**
	 * Returns the build settings for this build configuration.
	 *
	 * @return a {@link XcodeIdeBuildSettings} instance, never null.
	 */
	XcodeIdeBuildSettings getBuildSettings();

	/**
	 * Returns the product location built by Gradle.
	 *
	 * @return a property to configure the product location built by Gradle.
	 */
	Property<FileSystemLocation> getProductLocation();
}
