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
