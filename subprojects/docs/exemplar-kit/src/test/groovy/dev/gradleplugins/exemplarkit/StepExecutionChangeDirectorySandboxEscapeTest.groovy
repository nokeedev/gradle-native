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
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.StepExecutors.changeDirectory
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

class StepExecutionChangeDirectorySandboxEscapeTest {
	private static Step cd(String path) {
		return Step.builder().execute('cd', path).build()
	}

	@Test
	void "throws exception when changing directory outside of sandbox directory relatively"(@TempDir File testDirectory) {
		def context = new StepExecutionContext(testDirectory).forStep(cd('..'))
		def ex = assertThrows(IllegalStateException, {
			def result = changeDirectory().run(context)
			assertEquals(StepExecutionOutcome.FAILED, result.outcome)
			assertEquals('Cannot change directory outside of sandbox directory.', result.reason.orElse(null))
		})
		assertEquals('Cannot change directory outside of sandbox directory.', ex.message)
	}

	@Test
	void "throws exception when changing directory outside of sandbox directory absolute"(@TempDir File testDirectory) {
		def context = new StepExecutionContext(testDirectory).forStep(cd(testDirectory.parent))
		def ex = assertThrows(IllegalStateException, {
			def result = changeDirectory().run(context)
			assertEquals(StepExecutionOutcome.FAILED, result.outcome)
			assertEquals('Cannot change directory outside of sandbox directory.', result.reason.orElse(null))
		})
		assertEquals('Cannot change directory outside of sandbox directory.', ex.message)
	}

	@Test
	void "does not throw exception when changing directory back to sandbox directory relatively"(@TempDir File testDirectory) {
		given:
		def context = new StepExecutionContext(testDirectory)
			.currentWorkingDirectory(new File(testDirectory, 'foo'))
			.forStep(cd('..'))
		def result = changeDirectory().run(context)

		expect:
		assertEquals(StepExecutionOutcome.EXECUTED, result.outcome)
	}

	@Test
	void "does not throw exception when changing directory back to sandbox directory absolute"(@TempDir File testDirectory) {
		given:
		def context = new StepExecutionContext(testDirectory)
			.currentWorkingDirectory(new File(testDirectory, 'foo'))
			.forStep(cd(testDirectory.absolutePath))
		def result = changeDirectory().run(context)

		expect:
		assertEquals(StepExecutionOutcome.EXECUTED, result.outcome)
	}
}
