package dev.gradleplugins.exemplarkit.testers;

import dev.gradleplugins.exemplarkit.StepExecutionOutcome;
import dev.gradleplugins.exemplarkit.StepExecutionResult;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class StepExecutionResultSuccessfulStepTester extends AbstractStepExecutionResultTester {
	protected abstract StepExecutionResult getStepResultUnderTest();

	@Test
	void hasExecutedOutcome() {
		assertThat(getStepResultUnderTest().getOutcome(), equalTo(StepExecutionOutcome.EXECUTED));
	}

	@Test
	void hasExitValue() {
		assertTrue(getStepResultUnderTest().getExitValue().isPresent());
	}

	@Test
	void hasZeroExitValue() {
		assertThat(getStepResultUnderTest().getExitValue().get(), equalTo(0));
	}
}
