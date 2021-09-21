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
package dev.gradleplugins.exemplarkit


import dev.gradleplugins.exemplarkit.testers.StepExecutionResultFailureStepTester
import dev.gradleplugins.exemplarkit.testers.StepExecutionResultReasonTester
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.ExemplarRunner.create
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.iterableWithSize

class ExemplarRunnerExceptionDuringStepExecutionTest {
	@TempDir protected File testDirectory

	ExemplarExecutionResult result

	@BeforeEach
	void "given a single failing command exemplar"() {
		def exemplar = Exemplar.builder()
			.step(Step.builder().execute('ls'))
			.build()
		result = create(ExemplarExecutor.builder().defaultCommandLineToolExecutor(ExceptionThrowingStepExecutor.INSTANCE).build()).inDirectory(testDirectory).using(exemplar).run()
	}

	@Test
	void "has one step result"() {
		assertThat(result.stepResults, iterableWithSize(1))
	}

	@Nested
	class FailureStep extends StepExecutionResultFailureStepTester {
		@Override
		protected StepExecutionResult getStepResultUnderTest() {
			return result.stepResults.get(0)
		}
	}

	@Nested
	class FailureReasonStep extends StepExecutionResultReasonTester {
		@Override
		protected StepExecutionResult getStepResultUnderTest() {
			return result.stepResults.get(0)
		}

		@Override
		protected String getExpectedReason() {
			return 'A exception message'
		}
	}

	private enum ExceptionThrowingStepExecutor implements StepExecutor {
		INSTANCE;

		@Override
		boolean canHandle(Step step) {
			return true
		}

		@Override
		StepExecutionResult run(StepExecutionContext context) {
			throw new RuntimeException('A exception message')
		}
	}
}
