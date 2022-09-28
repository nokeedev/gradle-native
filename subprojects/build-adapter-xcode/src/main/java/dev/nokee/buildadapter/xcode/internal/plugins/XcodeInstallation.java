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
package dev.nokee.buildadapter.xcode.internal.plugins;

import java.nio.file.Path;

/**
 * Represent an Xcode installation.
 */
public interface XcodeInstallation {
	/**
	 * Returns the version of this Xcode installation.
	 *
	 * <p>Typically, the version is found using {@literal xcodebuild -version}.
	 *
	 * @return the version of this Xcode installation, never null
	 */
	String getVersion();

	/**
	 * Returns the developer directory (e.g. {@literal DEVELOPER_DIR}) of this Xcode installation.
	 *
	 * <p>Typically, the developer directory is found using {@literal xcode-select --print-path}.
	 *
	 * @return the developer directory of this Xcode installation, never null
	 */
	Path getDeveloperDirectory();
}
