package dev.gradleplugins.exemplarkit;

import java.util.List;

public final class ExemplarExecutionResult {
	private final List<StepExecutionResult> stepResults;

	ExemplarExecutionResult(List<StepExecutionResult> stepResults) {
		this.stepResults = stepResults;
	}

	public List<StepExecutionResult> getStepResults() {
		return stepResults;
	}
}
