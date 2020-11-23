package dev.gradleplugins.exemplarkit


import dev.gradleplugins.exemplarkit.testers.StepExecutionResultOutputTester
import dev.gradleplugins.exemplarkit.testers.StepExecutionResultSuccessfulStepTester
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.ExemplarExecutor.defaultExecutor
import static dev.gradleplugins.exemplarkit.ExemplarRunner.create
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.iterableWithSize

class ExemplarRunnerSingleOutputSuccessfulStepTest {
	@TempDir protected File testDirectory

	ExemplarExecutionResult result

	@BeforeEach
	void "given a single echo command exemplar"() {
		def exemplar = Exemplar.builder()
			.step(Step.builder().execute('echo', 'Hello, world!'))
			.build()
		result = create(defaultExecutor()).inDirectory(testDirectory).using(exemplar).run()
	}

	@Test
	void "has one step result"() {
		assertThat(result.stepResults, iterableWithSize(1))
	}

	@Nested
	class StepOutput extends StepExecutionResultOutputTester {
		@Override
		protected StepExecutionResult getStepResultUnderTest() {
			return result.stepResults.get(0)
		}

		@Override
		protected String getExpectedOutput() {
			return "Hello, world!${System.lineSeparator()}"
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
