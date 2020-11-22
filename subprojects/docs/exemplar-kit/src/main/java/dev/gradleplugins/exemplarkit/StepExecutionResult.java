package dev.gradleplugins.exemplarkit;

import javax.annotation.Nullable;
import java.util.Optional;

public final class StepExecutionResult {
	private final StepExecutionOutcome outcome;
	@Nullable private final String reason;
	@Nullable private final Integer exitValue;
	@Nullable private final String output;

	private StepExecutionResult(StepExecutionOutcome outcome, @Nullable String reason, @Nullable Integer exitValue, @Nullable String output) {
		this.outcome = outcome;
		this.reason = reason;
		this.exitValue = exitValue;
		this.output = output;
	}

	public static StepExecutionResult stepFailed(Throwable e) {
		return new StepExecutionResult(StepExecutionOutcome.FAILED, e.getMessage(), null, null);
	}

	public StepExecutionOutcome getOutcome() {
		return outcome;
	}

	public Optional<String> getReason() {
		return Optional.ofNullable(reason);
	}

	public Optional<Integer> getExitValue() {
		return Optional.ofNullable(exitValue);
	}

	public Optional<String> getOutput() {
		return Optional.ofNullable(output);
	}

	public static StepExecutionResult stepSkipped() {
		return new StepExecutionResult(StepExecutionOutcome.SKIPPED, "A skipping condition was matched", null, null);
	}

	public static StepExecutionResult stepExecuted(int exitValue) {
		return new StepExecutionResult(StepExecutionOutcome.EXECUTED, null, exitValue, null);
	}

	public static StepExecutionResult stepExecuted(int exitValue, String output) {
		return new StepExecutionResult(StepExecutionOutcome.EXECUTED, null, exitValue, output);
	}
}
