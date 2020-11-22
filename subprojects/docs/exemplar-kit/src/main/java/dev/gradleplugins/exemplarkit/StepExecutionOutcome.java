package dev.gradleplugins.exemplarkit;

/**
 * The execution outcome of a step from an exemplar.
 */
public enum StepExecutionOutcome {
	/**
	 * The step executed either successfully or not.
	 */
	EXECUTED,

	/**
	 * The step failed for a reason other then the executable returning a non-zero exit value.
	 */
	FAILED,

	/**
	 * The step was not executed for some reason.
	 */
	SKIPPED
}
