/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.internal.testing.util;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;

public final class ConfigurationTestUtils {
	private static final String TEST_CONFIGURATION_NAME = "test";
	private ConfigurationTestUtils() {}

	public static Configuration testConfiguration() {
		return testConfiguration(TEST_CONFIGURATION_NAME);
	}

	public static Configuration testConfiguration(String name) {
		return rootProject().getConfigurations().create(name);
	}

	public static Configuration testConfiguration(Action<? super Configuration> action) {
		return testConfiguration(TEST_CONFIGURATION_NAME, action);
	}

	public static Configuration testConfiguration(String name, Action<? super Configuration> action) {
		val configuration = testConfiguration(name);
		action.execute(configuration);
		return configuration;
	}
}
