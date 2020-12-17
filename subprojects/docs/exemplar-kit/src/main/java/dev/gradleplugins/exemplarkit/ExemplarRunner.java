package dev.gradleplugins.exemplarkit;

import java.io.File;
import java.util.Objects;

public final class ExemplarRunner {
	private File workingDirectory;
	private Exemplar exemplar;
	private ExemplarExecutor executor;

	private ExemplarRunner(ExemplarExecutor executor) {
		this.executor = executor;
	}

	public static ExemplarRunner create(ExemplarExecutor executor) {
		return new ExemplarRunner(executor);
	}

	public ExemplarRunner inDirectory(File workingDirectory) {
		this.workingDirectory = Objects.requireNonNull(workingDirectory, "Please specify a non-null working directory.");
		return this;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public ExemplarRunner using(Exemplar exemplar) {
		this.exemplar = Objects.requireNonNull(exemplar, "Please specify a non-null exemplar.");
		return this;
	}

	public ExemplarExecutionResult run() throws InvalidRunnerConfigurationException {
		if (workingDirectory == null) {
			throw new InvalidRunnerConfigurationException("Please specify a working directory using ExemplarRunner#inDirectory(File).");
		}

		if (exemplar == null) {
			throw new InvalidRunnerConfigurationException("Please specify an exemplar using ExemplarRunner#using(Exemplar).");
		}

		return executor.run(new ExemplarExecutionContext(workingDirectory, exemplar));
	}
}
