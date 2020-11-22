package dev.gradleplugins.exemplarkit.testers;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class StepExecutionResultReasonTester extends AbstractStepExecutionResultTester {
	protected abstract String getExpectedReason();

	@Test
	void hasReason() {
		assertThat(getStepResultUnderTest().getReason().orElse(null), equalTo(getExpectedReason()));
	}
}
