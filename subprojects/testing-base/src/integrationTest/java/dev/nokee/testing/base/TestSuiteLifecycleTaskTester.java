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

package dev.nokee.testing.base;

import dev.nokee.testing.base.internal.tasks.TestSuiteLifecycleTask;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

public abstract class TestSuiteLifecycleTaskTester {
	public abstract TestSuiteComponent testSuite();

	public TestSuiteLifecycleTask subject() {
		return project().getTasks().withType(TestSuiteLifecycleTask.class).getByName(testSuite().getName());
	}

	public abstract Project project();

	@Test
	void hasTestSuiteLifecycleTaskMatchingItsName() {
		assertThat(project().getTasks(), hasItem(allOf(named(testSuite().getName()), isA(TestSuiteLifecycleTask.class))));
	}

	@Test
	void hasTestSuite() {
		assertThat(subject().getTestSuite(), providerOf(testSuite()));
	}

	@Test
	void hasDescription() {
		assertThat(subject().getDescription(), equalTo("Runs the test suite ':" + testSuite().getName() + "'."));
	}

	@Test
	void hasVerificationGroup() {
		assertThat(subject().getGroup(), equalTo("verification"));
	}
}
