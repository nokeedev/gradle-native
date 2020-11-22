package dev.gradleplugins.exemplarkit;

/**
 * Executor handling one or several steps.
 */
public interface StepExecutor {
	boolean canHandle(Step step);
	StepExecutionResult run(StepExecutionContext context);
}
