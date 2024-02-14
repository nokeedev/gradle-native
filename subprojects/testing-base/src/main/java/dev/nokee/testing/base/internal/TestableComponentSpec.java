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

import dev.nokee.model.internal.discover.DisRule;
import dev.nokee.model.internal.discover.Discover;
import dev.nokee.model.internal.discover.Discovery;
import dev.nokee.model.internal.discover.SubTypeOfRule;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.Component;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.TestableComponent;
import org.gradle.api.plugins.ExtensionAware;

import java.util.Collections;
import java.util.List;

@Discover(TestableComponentSpec.Strategy.class)
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

	final class Strategy implements Discovery {
		@Override
		public <T> List<DisRule> discover(ModelType<T> discoveringType) {
			return Collections.singletonList(new SubTypeOfRule(discoveringType, new DisRule() {
				@Override
				public void execute(Details details) {
					// Assuming all registration happens during realization
					if (!details.getCandidate().isRealized()) {
						details.newCandidate(ElementName.of("*"), ModelType.of(TestSuiteComponent.class));
					}
				}
			}));
		}
	}
}
