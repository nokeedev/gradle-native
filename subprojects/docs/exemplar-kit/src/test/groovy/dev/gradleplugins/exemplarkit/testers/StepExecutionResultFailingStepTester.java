/*
 * Copyright 2020 the original author or authors.
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
package dev.gradleplugins.exemplarkit.testers;

import dev.gradleplugins.exemplarkit.StepExecutionOutcome;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class StepExecutionResultFailingStepTester extends AbstractStepExecutionResultTester {
	@Test
	void hasExecutedOutcome() {
		assertThat(getStepResultUnderTest().getOutcome(), equalTo(StepExecutionOutcome.EXECUTED));
	}

	@Test
	void hasExitValue() {
		assertTrue(getStepResultUnderTest().getExitValue().isPresent());
	}

	@Test
	void hasNonZeroExitValue() {
		assertThat(getStepResultUnderTest().getExitValue().get(), not(equalTo(0)));
	}
}
