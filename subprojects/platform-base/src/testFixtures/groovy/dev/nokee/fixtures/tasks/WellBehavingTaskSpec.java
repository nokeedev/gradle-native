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
package dev.nokee.fixtures.tasks;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;

public interface WellBehavingTaskSpec {
	TestFile getTestDirectory();

	/**
	 * Creates file referenced by the path relative to the {@link WellBehavingTaskSpec#getTestDirectory} directory.
	 * @param path a relative path from {@link WellBehavingTaskSpec#getTestDirectory}
	 * @return a {@link TestFile} instance of the specified path
	 */
	TestFile file(Object... path);

	/**
	 * @return the build file for the project declaring the task under test
	 */
	TestFile getBuildFile();

	/**
	 * @return a {@link ExecutionResult} instance representing the last Gradle execution
	 * @see dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
	 */
	ExecutionResult getResult();

	void assertInitialState();

	static WellBehavingTaskAssertion taskUnderTestExecutedAndNotSkipped() {
		return new WellBehavingTaskAssertion() {
			@Override
			public String getDescription() {
				return "':taskUnderTest' has executed and was not skipped";
			}

			@Override
			public void assertState(WellBehavingTaskSpec context) {
				context.getResult().assertTasksExecutedAndNotSkipped(":taskUnderTest");
			}
		};
	}

	static WellBehavingTaskAssertion taskUnderTestCached() {
		// TODO: Technically not asserting that it was cached...
		return new WellBehavingTaskAssertion() {
			@Override
			public String getDescription() {
				return "':taskUnderTest' was skipped due to caching";
			}

			@Override
			public void assertState(WellBehavingTaskSpec context) {
				context.getResult().assertTasksSkipped(":taskUnderTest");
			}
		};
	}

	static WellBehavingTaskAssertion taskUnderTestNoSource() {
		// TODO: Technically not asserting that it was no source...
		return new WellBehavingTaskAssertion() {
			@Override
			public String getDescription() {
				return "':taskUnderTest' was skipped due to no source";
			}

			@Override
			public void assertState(WellBehavingTaskSpec context) {
				context.getResult().assertTasksSkipped(":taskUnderTest");
			}
		};
	}
}
