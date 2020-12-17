package dev.gradleplugins.exemplarkit


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.StepExecutors.changeDirectory
import static org.junit.jupiter.api.Assertions.assertAll
import static org.junit.jupiter.api.Assertions.assertEquals

class StepExecutionChangeDirectoryTest {
	@TempDir
	protected File testDirectory

	private static Step cd(String path) {
		return Step.builder().execute('cd', path).build()
	}

	@Test
	void "fail step when canonicalize sandbox directory"() {
		def context = new StepExecutionContext(new ThrowExceptionWhenCanonicalizeFile(testDirectory))
			.currentWorkingDirectory(testDirectory)
			.forStep(cd('foo'))
		def result = changeDirectory().run(context)
		assertAll({
			assertEquals(StepExecutionOutcome.FAILED, result.outcome)
			assertEquals("Could not canonicalize.", result.reason.orElse(null))
		} as Executable)
	}

	private static class ThrowExceptionWhenCanonicalizeFile extends File {
		ThrowExceptionWhenCanonicalizeFile(File file) {
			super(file.absolutePath)
		}

		@Override
		File getCanonicalFile() throws IOException {
			throw new IOException("Could not canonicalize.")
		}
	}
}
