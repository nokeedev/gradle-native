package dev.gradleplugins.exemplarkit.testers;

import dev.gradleplugins.exemplarkit.StepExecutionOutcome;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class StepExecutionResultFailingStepTester extends AbstractStepExecutionResultTester {
	@Test
	void hasExecutedOutcome() {
		assertThat(getStepResultUnderTest().getOutcome(), equalTo(StepExecutionOutcome.EXECUTED));
	}

	@Test
	void hasExitValue() {
		assertTrue(getStepResultUnderTest().getExitValue().isPresent());
	}

	@Test
	void hasNonZeroExitValue() {
		assertThat(getStepResultUnderTest().getExitValue().get(), not(equalTo(0)));
	}
}
