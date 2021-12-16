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
package dev.nokee.platform.jni;

import dev.nokee.internal.testing.WellBehavedPluginTester;
import dev.nokee.internal.testing.util.TestCaseUtils;
import dev.nokee.platform.jni.internal.plugins.JniLibraryBasePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class JniLibraryBasePluginWellBehavedPluginTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.qualifiedPluginId("dev.nokee.jni-library-base")
			.pluginClass(JniLibraryBasePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
