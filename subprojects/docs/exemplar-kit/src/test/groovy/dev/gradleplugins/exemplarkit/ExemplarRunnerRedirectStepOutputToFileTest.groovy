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
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.iterableWithSize

class ExemplarRunnerRedirectStepOutputToFileTest {
	@TempDir protected File testDirectory

	ExemplarExecutionResult result

	@BeforeEach
	void "given a single echo command redirecting output exemplar"() {
		def exemplar = Exemplar.builder()
			.step(Step.builder().execute('echo', '"Hello, world!"', '>', 'foo.txt'))
			.build()
		result = create(defaultExecutor()).inDirectory(testDirectory).using(exemplar).run()
	}

	@Test
	void "has one step result"() {
		assertThat(result.stepResults, iterableWithSize(1))
	}

	@Test
	void "redirect output to file"() {
		assertThat(new File(testDirectory, 'foo.txt').text, equalTo("Hello, world!${System.lineSeparator()}".toString()))
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
