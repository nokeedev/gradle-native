package dev.gradleplugins.exemplarkit

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.StepExecutors.changeDirectory
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.not
import static org.junit.jupiter.api.Assertions.assertEquals

class StepExecutionChangeDirectoryNonExistentTest {
	@TempDir
	protected static File testDirectory
	static StepExecutionResult result
	static StepExecutionContext context

	@BeforeAll
	static void "change to non-existent directory"() {
		context = new StepExecutionContext(testDirectory).forStep(cd('foo'))
		result = changeDirectory().run(context)
	}

	private static Step cd(String path) {
		return Step.builder().execute('cd', path).build()
	}

	@Test
	void "has executed outcome"() {
		assertEquals(StepExecutionOutcome.EXECUTED, result.outcome)
	}

	@Test
	void "has non-zero exit value"() {
		assertThat(result.exitValue, not(equalTo(0)))
	}

	@Test
	void "does not change current working directory"() {
		assertEquals(testDirectory.canonicalFile, context.currentWorkingDirectory.canonicalFile)
	}
}
