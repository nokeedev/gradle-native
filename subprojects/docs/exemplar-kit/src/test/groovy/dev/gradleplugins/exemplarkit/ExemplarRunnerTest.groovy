package dev.gradleplugins.exemplarkit

import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.ExemplarExecutor.defaultExecutor
import static dev.gradleplugins.exemplarkit.ExemplarRunner.create
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.junit.jupiter.api.Assertions.assertTrue

class ExemplarRunnerTest {
	@TempDir
	protected File testDirectory

	@Test
	void "can execute empty exemplar"() {
		def exemplar = Exemplar.builder().build()
		def result = create(defaultExecutor()).inDirectory(testDirectory).using(exemplar).run()
		assertThat(result.stepResults, Matchers.emptyIterable())
	}

	@Nested
	class StepOnSampleFromDirectory {
		@TempDir protected File testDirectory

		ExemplarExecutionResult result

		@BeforeEach
		void "an exemplar"() {
			def sampleDirectory = new File(testDirectory, 'sample')
			sampleDirectory.mkdir()
			new File(sampleDirectory, 'foo.txt') << '''In a galaxy far far away,
				|Tada, da da ta da ta
				|Tada da da
				|'''.stripMargin()

			def workingDirectory = new File(testDirectory, 'working-dir')
			workingDirectory.mkdir()

			def exemplar = Exemplar.builder()
				.fromDirectory(sampleDirectory)
				.step(Step.builder().execute('cat', 'foo.txt'))
				.build()
			result = create(defaultExecutor()).inDirectory(workingDirectory).using(exemplar).run()
		}

		@Test
		void "has one step result"() {
			assertThat(result.stepResults, Matchers.iterableWithSize(1))
		}

		@Test
		void "has step output"() {
			assertTrue(result.stepResults.get(0).output.present)
			assertThat(result.stepResults.get(0).output.get(), equalTo('In a galaxy far far away,\nTada, da da ta da ta\nTada da da\n'))
		}

		@Test
		void "has executed outcome"() {
			assertThat(result.stepResults.get(0).outcome, equalTo(StepExecutionOutcome.EXECUTED))
		}

		@Test
		void "has exit value"() {
			assertTrue(result.stepResults.get(0).exitValue.present)
			assertThat(result.stepResults.get(0).exitValue.get(), equalTo(0))
		}
	}
}
