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
package dev.gradleplugins.grava.testing;

import dev.gradleplugins.grava.testing.fixtures.WellBehavedTestPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.invocation.Gradle;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.WellBehavedPluginTester.SupportedTarget.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class WellBehavedInitPluginTesterIntegrationTest {
	@Test
	void checkWellBehavedPlugin() {
		assertDoesNotThrow(new WellBehavedPluginTester()
			.pluginClass(InitPlugin.class)
			.supportedTarget(Init)
			.doesNotWellBehaveWhenAppliedToUnsupportedTarget()::testWellBehavedPlugin
		);
	}

	@Test
	void skipsQualifiedPluginIdChecksForInitTarget() {
		assertDoesNotThrow(new WellBehavedPluginTester()
			.qualifiedPluginId("dev.gradleplugins.gravatesting.well-behaved-plugin")
			.pluginClass(WellBehavedTestPlugin.class)
			.supportedTarget(Init, Project, Settings)::testWellBehavedPlugin);
	}

	public static class InitPlugin implements Plugin<Gradle> {
		@Override
		public void apply(Gradle target) {}
	}
}
