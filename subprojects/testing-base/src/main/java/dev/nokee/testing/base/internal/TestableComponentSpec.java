/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.testing.base.internal;

import dev.nokee.platform.base.Component;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.TestableComponent;
import org.gradle.api.plugins.ExtensionAware;

public interface TestableComponentSpec extends TestableComponent, Component, ExtensionAware {
	String TEST_SUITES_EXTENSION_NAME = "testSuites";

	@Override
	default TestSuiteContainer getTestSuites() {
		final TestSuiteContainer testSuites = (TestSuiteContainer) getExtensions().findByName(TEST_SUITES_EXTENSION_NAME);
		if (testSuites == null) {
			throw new RuntimeException("Please apply 'dev.nokee.testing-base' plugin before using test suites.");
		}
		return testSuites;
	}
}
