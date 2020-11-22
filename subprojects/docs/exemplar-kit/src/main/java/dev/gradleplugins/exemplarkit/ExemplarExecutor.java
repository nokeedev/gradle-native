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
