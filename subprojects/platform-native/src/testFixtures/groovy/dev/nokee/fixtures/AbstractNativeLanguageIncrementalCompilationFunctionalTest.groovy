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
package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.NativeProjectTasks

abstract class AbstractNativeLanguageIncrementalCompilationFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "skip assemble tasks when no source"() {
		given:
		makeSingleProject()

		expect:
		succeeds "assemble"
		result.assertTasksExecuted(taskNamesUnderTest.allToLifecycleAssemble)
		result.assertTasksSkipped(allSkippedTasksToAssemble)
	}

	def "skip objects tasks when no source"() {
		given:
		makeSingleProject()

		expect:
		succeeds "objects"
		result.assertTasksExecuted(taskNamesUnderTest.allToLifecycleObjects)
		result.assertTasksSkipped(taskNamesUnderTest.allToLifecycleObjects)
	}

	protected abstract void makeSingleProject()

	protected abstract String getBinaryLifecycleTaskName()

	protected List<String> getAllSkippedTasksToAssemble() {
		return taskNamesUnderTest.allToLifecycleAssemble
	}

	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks
	}
}
