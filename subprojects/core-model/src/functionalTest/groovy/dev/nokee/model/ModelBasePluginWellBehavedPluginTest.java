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
package dev.nokee.model;

import dev.nokee.internal.testing.WellBehavedPluginTester;
import dev.nokee.internal.testing.util.TestCaseUtils;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static dev.nokee.internal.testing.WellBehavedPluginTester.SupportedTarget.Project;
import static dev.nokee.internal.testing.WellBehavedPluginTester.SupportedTarget.Settings;

class ModelBasePluginWellBehavedPluginTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.supportedTarget(Settings, Project)
			.qualifiedPluginId("dev.nokee.model-base")
			.pluginClass(ModelBasePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
