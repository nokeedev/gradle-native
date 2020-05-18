package dev.nokee.platform.ios.tasks.fixtures;

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
