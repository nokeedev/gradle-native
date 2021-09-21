/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
