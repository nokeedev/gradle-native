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

import dev.nokee.internal.testing.TestCase;
import org.junit.jupiter.api.DynamicTest;

public final class TestCaseUtils {
	private TestCaseUtils() {}

	/**
	 * Convert Grava test case to JUnit's dynamic test to use within a test factory scenario.
	 *
	 * <pre>
	 * class FooTest {
	 *     &#64;TestFactory
	 *     Stream&#60;DynamicTest&#62; checkWellBehavedPlugin() {
	 *         return new WellBehavedPluginTester()
	 *             .pluginType(WellBehavedTestPlugin.class)
	 *             .qualifiedPluginId("gravatesting.well-behaved-plugin")
	 *             .stream()
	 *             .map(TestCaseUtils::toJUnit5DynamicTest);
	 *     }
	 * }
	 * </pre>
	 * @param testCase  a test case to convert into {@link DynamicTest}, must not be null
	 * @return a JUnit 5 dynamic test, never null
	 */
	public static DynamicTest toJUnit5DynamicTest(TestCase testCase) {
		return DynamicTest.dynamicTest(testCase.getDisplayName(), () -> {
			testCase.setUp();
			try {
				testCase.execute();
			} finally {
				testCase.tearDown();
			}
		});
	}
}
