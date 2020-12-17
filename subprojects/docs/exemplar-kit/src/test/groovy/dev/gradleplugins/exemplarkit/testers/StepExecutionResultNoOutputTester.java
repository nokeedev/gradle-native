package dev.gradleplugins.exemplarkit.testers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public abstract class StepExecutionResultNoOutputTester extends AbstractStepExecutionResultTester {
	@Test
	void hasNoOutput() {
		assertFalse(getStepResultUnderTest().getOutput().isPresent(), () -> "has output, expecting none:\n" + getStepResultUnderTest().getOutput().get());
	}
}
