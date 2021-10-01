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

import dev.nokee.internal.testing.WellBehavedPluginTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Sometime, a plugin maybe injected in-between the plugin id and the plugin type for backward compatibility reason.
 * In those cases, it is important the plugin is always applied via its id and not by type.
 * In other cases, we may not really care about the plugin type being applied and may only want to test the plugin by id.
 */
class PluginIdOnlyWellBehavedPluginTesterIntegrationTest {
	@Test
	void failsWhenMisbehavePluginByIdOnly() {
		val ex = assertThrows(AssertionError.class, new WellBehavedPluginTester().qualifiedPluginId("dev.gradleplugins.gravatesting.misbehaved-plugin")::testWellBehavedPlugin);
		assertThat(ex.getMessage(), allOf(startsWith("Plugin is not well-behaved"), containsString("does not resolve configuration")));
	}
}
