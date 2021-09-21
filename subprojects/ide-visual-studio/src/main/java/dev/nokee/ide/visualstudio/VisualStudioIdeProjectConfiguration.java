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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * The project configuration each item configuration in Visual Studio projects.
 * It associate a single configuration, i.e. Default, Debug, Release, with a single platform, i.e. x64, Win32, ARM.
 *
 * @since 0.5
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VisualStudioIdeProjectConfiguration {
	VisualStudioIdeConfiguration configuration;
	VisualStudioIdePlatform platform;

	public VisualStudioIdeConfiguration getConfiguration() {
		return configuration;
	}

	public VisualStudioIdePlatform getPlatform() {
		return platform;
	}

	public static VisualStudioIdeProjectConfiguration of(VisualStudioIdeConfiguration configuration, VisualStudioIdePlatform platform) {
		return new VisualStudioIdeProjectConfiguration(configuration, platform);
	}

	@Override
	public String toString() {
		return String.format("project configuration '%s|%s'", configuration, platform);
	}
}
