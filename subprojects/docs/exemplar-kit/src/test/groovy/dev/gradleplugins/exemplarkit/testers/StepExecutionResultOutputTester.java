package dev.gradleplugins.exemplarkit.testers;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class StepExecutionResultOutputTester extends AbstractStepExecutionResultTester {
	protected abstract String getExpectedOutput();

	@Test
	void hasOutput() {
		assertTrue(getStepResultUnderTest().getOutput().isPresent());

	}

	@Test
	void hasExpectedOutput() {
		assertThat(getStepResultUnderTest().getOutput().get(), equalTo(getExpectedOutput()));
	}
}
