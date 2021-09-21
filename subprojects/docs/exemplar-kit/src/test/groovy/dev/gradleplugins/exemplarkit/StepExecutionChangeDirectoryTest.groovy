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
