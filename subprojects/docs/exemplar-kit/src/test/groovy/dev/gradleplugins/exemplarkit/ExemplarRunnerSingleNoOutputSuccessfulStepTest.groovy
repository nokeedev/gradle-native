package dev.gradleplugins.exemplarkit

import dev.gradleplugins.exemplarkit.testers.StepExecutionResultNoOutputTester
import dev.gradleplugins.exemplarkit.testers.StepExecutionResultSuccessfulStepTester
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.ExemplarExecutor.defaultExecutor
import static dev.gradleplugins.exemplarkit.ExemplarRunner.create
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.iterableWithSize

class ExemplarRunnerSingleNoOutputSuccessfulStepTest {
	@TempDir protected File testDirectory

	ExemplarExecutionResult result

	@BeforeEach
	void "given a single command without output exemplar"() {
		def exemplar = Exemplar.builder()
			.step(Step.builder().execute('true'))
			.build()
		result = create(defaultExecutor()).inDirectory(testDirectory).using(exemplar).run()
	}

	@Test
	void "has one step result"() {
		assertThat(result.stepResults, iterableWithSize(1))
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
