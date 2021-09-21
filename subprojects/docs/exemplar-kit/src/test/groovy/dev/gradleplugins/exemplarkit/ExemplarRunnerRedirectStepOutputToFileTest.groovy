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
import dev.gradleplugins.exemplarkit.testers.StepExecutionResultSuccessfulStepTester
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.ExemplarExecutor.defaultExecutor
import static dev.gradleplugins.exemplarkit.ExemplarRunner.create
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.iterableWithSize

class ExemplarRunnerRedirectStepOutputToFileTest {
	@TempDir protected File testDirectory

	ExemplarExecutionResult result

	@BeforeEach
	void "given a single echo command redirecting output exemplar"() {
		def exemplar = Exemplar.builder()
			.step(Step.builder().execute('echo', 'Hello, world!', '>', 'foo.txt'))
			.build()
		result = create(defaultExecutor()).inDirectory(testDirectory).using(exemplar).run()
	}

	@Test
	void "has one step result"() {
		assertThat(result.stepResults, iterableWithSize(1))
	}

	@Test
	@EnabledOnOs([OS.WINDOWS])
	void "redirect output to file on Windows"() {
		assertThat(new File(testDirectory, 'foo.txt').text, equalTo('Hello, world! \r\n'))
	}

	@Test
	@DisabledOnOs([OS.WINDOWS])
	void "redirect output to file on *nix"() {
		assertThat(new File(testDirectory, 'foo.txt').text, equalTo('Hello, world!\n'))
	}

	@Nested
	class StepNoOutput extends StepExecutionResultNoOutputTester {
		@Override
		protected StepExecutionResult getStepResultUnderTest() {
			return result.stepResults.get(0)
		}
	}

	@Nested
	class SuccessfulStep extends StepExecutionResultSuccessfulStepTester {
		@Override
		protected StepExecutionResult getStepResultUnderTest() {
			return result.stepResults.get(0)
		}
	}
}
