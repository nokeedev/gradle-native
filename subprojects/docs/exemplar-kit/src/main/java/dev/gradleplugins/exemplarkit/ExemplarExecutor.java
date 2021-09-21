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

import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.gradleplugins.exemplarkit.StepExecutionResult.stepFailed;

public final class ExemplarExecutor {
	private final List<StepExecutor> executors;
	private final StepExecutor defaultExecutor;

	private ExemplarExecutor(List<StepExecutor> executors, StepExecutor defaultExecutor) {
		this.executors = executors;
		this.defaultExecutor = defaultExecutor;
	}

	public static ExemplarExecutor defaultExecutor() {
		return builder().registerCommandLineToolExecutor(StepExecutors.changeDirectory()).build();
	}

	ExemplarExecutionResult run(ExemplarExecutionContext context) {
		context.getExemplar().getSample().writeToDirectory(context.getWorkingDirectory());

		val stepContext = new StepExecutionContext(context.getWorkingDirectory());
		val stepResults = context.getExemplar().getSteps().stream().map(it -> runStep(stepContext.forStep(it))).collect(Collectors.toList());

		return new ExemplarExecutionResult(stepResults);
	}

	private StepExecutionResult runStep(StepExecutionContext context) {
		val executor = executors.stream().filter(it -> it.canHandle(context.getCurrentStep())).findFirst().orElse(defaultExecutor);

		try {
			return executor.run(context);
		} catch (RuntimeException e) {
			return stepFailed(e);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final List<StepExecutor> executors = new ArrayList<>();
		private StepExecutor defaultExecutor = StepExecutors.hostTerminal();

		public Builder registerCommandLineToolExecutor(StepExecutor executor) {
			executors.add(executor);
			return this;
		}

		public Builder defaultCommandLineToolExecutor(StepExecutor executor) {
			this.defaultExecutor = executor;
			return this;
		}

		public ExemplarExecutor build() {
			return new ExemplarExecutor(executors, defaultExecutor);
		}
	}
}
