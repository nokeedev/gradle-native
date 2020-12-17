package dev.gradleplugins.exemplarkit.testers;

import dev.gradleplugins.exemplarkit.StepExecutionOutcome;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

public abstract class StepExecutionResultFailureStepTester extends AbstractStepExecutionResultTester {
	@Test
	void hasFailedOutcome() {
		assertThat(getStepResultUnderTest().getOutcome(), equalTo(StepExecutionOutcome.FAILED));
	}

	@Test
	void hasNoExitValue() {
		assertFalse(getStepResultUnderTest().getExitValue().isPresent());
	}


}
