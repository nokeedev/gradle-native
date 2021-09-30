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

import dev.gradleplugins.grava.testing.fixtures.ThrowingTestPlugin;
import dev.gradleplugins.grava.testing.fixtures.WellBehavedTestPlugin;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.opentest4j.MultipleFailuresError;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WellBehavedPluginTesterIntegrationTest {
	@Test
	void failsIfPluginIdPointsToMissingClass() {
		val ex = assertThrows(MultipleFailuresError.class,
			new WellBehavedPluginTester().qualifiedPluginId("dev.gradleplugins.gravatesting.missing-class").pluginClass(WellBehavedTestPlugin.class)::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("can apply plugin by id using apply(plugin: <id>)")));
	}

	@Test
	void failsIfPluginThrowsException() {
		val ex = assertThrows(MultipleFailuresError.class,
			new WellBehavedPluginTester().pluginClass(ThrowingTestPlugin.class)::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("can apply plugin by id using apply(plugin: <class>)")));
	}
}
