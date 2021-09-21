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

import dev.gradleplugins.exemplarkit.testers.StepExecutionResultNoOutputTester
import dev.gradleplugins.exemplarkit.testers.StepExecutionResultOutputTester
import dev.gradleplugins.exemplarkit.testers.StepExecutionResultSuccessfulStepTester
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.SystemUtils
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.ExemplarExecutor.defaultExecutor
import static dev.gradleplugins.exemplarkit.ExemplarRunner.create
import static org.hamcrest.MatcherAssert.assertThat

class ExemplarRunnerChangeDirectoryStepTest {
	@TempDir protected File testDirectory

	ExemplarExecutionResult result

	@BeforeEach
	void "an exemplar"() {
		new File(testDirectory, 'nested/dir').mkdirs()

		def exemplar = Exemplar.builder()
			.step(Step.builder().execute('cd', 'nested/dir'))
			.step(Step.builder().execute(SystemUtils.IS_OS_WINDOWS ? 'cd' : 'pwd'))
			.build()
		result = create(defaultExecutor()).inDirectory(testDirectory).using(exemplar).run()
	}

	@Test
	void "has two step result"() {
		assertThat(result.stepResults, Matchers.iterableWithSize(2))
	}

	@Nested
	class FirstStep {
		@Nested
		class SuccessfulStep extends StepExecutionResultSuccessfulStepTester {
			@Override
			protected StepExecutionResult getStepResultUnderTest() {
				return result.stepResults.get(0)
			}
		}

		@Nested
		class NoOutputStep extends StepExecutionResultNoOutputTester {
			@Override
			protected StepExecutionResult getStepResultUnderTest() {
				return result.stepResults.get(0)
			}
		}
	}

	@Nested
	class SecondStep {
		@Nested
		class SuccessfulStep extends StepExecutionResultSuccessfulStepTester {
			@Override
			protected StepExecutionResult getStepResultUnderTest() {
				return result.stepResults.get(1)
			}
		}

		@Nested
		class OutputStep extends StepExecutionResultOutputTester {
			@Override
			protected StepExecutionResult getStepResultUnderTest() {
				return result.stepResults.get(1)
			}

			@Override
			protected String getExpectedOutput() {
				return "${FilenameUtils.separatorsToSystem(testDirectory.canonicalPath + '/nested/dir')}\n"
			}
		}
	}
}
